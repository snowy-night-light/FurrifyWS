/*
 * furrify-attachment-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.attachment.service.file;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.domain.file.strategy.hash.AttachmentFileHashStrategy;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashTarget;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;
import ws.furrify.attachment.dto.file.vo.AttachmentFileHashDTO;
import ws.furrify.attachment.exception.AttachmentErrors;
import ws.furrify.attachment.service.file.jxl.JxlConverter;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Uploads: hashing, metadata, JPEG XL conversion and storing the file. That work runs outside any
 * transaction (it can take minutes); the database steps are calls to {@link AttachmentFileEntityService},
 * each its own short transaction through that bean's proxy.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentFileProcessorService {

    private final AttachmentFileEntityService entityService;
    private final Set<AttachmentFileHashStrategy> hashStrategies;
    private final FileMassStorageStrategy fileMassStorageStrategy;
    private final Tika tika;
    private final JxlConverter jxlConverter;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AttachmentFileDTO createWithFileUpload(AttachmentFileDTO dto, MultipartFile multipartFile) {

        dto.setUploadStatus(FileUploadStatus.NOT_UPLOADED);

        File tempFile = null;
        AttachmentFileDTO createdDto = null;
        try {
            tempFile = Files.createTempFile("upload-", multipartFile.getOriginalFilename()).toFile();
            multipartFile.transferTo(tempFile);

            extractFileMetadataToDto(dto, tempFile);
            generateFileHashes(dto, tempFile);

            createdDto = entityService.create(dto);
            return uploadFile(createdDto, tempFile, false);
        } catch (Exception e) {
            log.warn("Failed to process attachment file uploaded data.", e);

            if (createdDto != null && createdDto.getId() != null) {
                UUID createdId = createdDto.getId();
                try {
                    entityService.deleteById(createdId);
                } catch (Exception cleanup) {
                    log.warn("Failed to remove attachment record {} after a failed upload: {}", createdId, cleanup.getMessage());
                }
            }

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(dto.getFileName()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AttachmentFileDTO patchWithFileUpload(UUID id, PatchAttachmentFileRequest patchDto, @NotNull MultipartFile multipartFile) {
        AttachmentFileDTO patchedDto = entityService.applyPatch(id, patchDto);

        File tempFile = null;
        try {
            tempFile = Files.createTempFile("upload-", multipartFile.getOriginalFilename()).toFile();
            multipartFile.transferTo(tempFile);

            extractFileMetadataToDto(patchedDto, tempFile);
            generateFileHashes(patchedDto, tempFile);

            return uploadFile(patchedDto, tempFile, true);
        } catch (Exception e) {
            log.warn("Failed to process attachment file uploaded data.", e);

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(patchedDto.getFileName()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /** Store the file (or link an identical stored one) and record it */
    private AttachmentFileDTO uploadFile(AttachmentFileDTO dto, File file, boolean replaceExisting) {
        File converted = null;
        try {
            UploadedFileReference uploadedFileRef = null;
            long fileSize = 0;

            // An identical file (by the hash of the uploaded bytes or of a stored JPEG XL) is linked, not stored again.
            AttachmentFileDTO existingFile = findDuplicate(dto.getFileHashes());

            if (existingFile != null && existingFile.getFileUri() != null) {
                adoptStoredFormat(dto, existingFile);
                try {
                    uploadedFileRef = fileMassStorageStrategy.linkFile(dto.getId(), dto.getMimeType(), existingFile.getFileUri(), existingFile.getThumbnailUri());
                    // the size of the stored file it links to (before the upload is deleted, for the fallback)
                    fileSize = existingFile.getFileSize() != null ? existingFile.getFileSize() : file.length();
                    file.delete();
                } catch (Exception linkException) {
                    log.warn("Failed to link existing file {}. Falling back to normal upload. Reason: {}", existingFile.getId(), linkException.getMessage());
                    fileMassStorageStrategy.removeFileDirectory(dto.getId());
                    restoreUploadedFormat(dto, existingFile);
                }
            }
            if (uploadedFileRef == null) {
                String uploadedMimeType = dto.getMimeType();
                converted = convertToJxl(dto, file);
                File stored = converted != null ? converted : file;
                fileSize = stored.length(); // before the file is moved into storage
                uploadedFileRef = fileMassStorageStrategy.uploadFile(dto.getId(), stored, file, uploadedMimeType, replaceExisting);
            }

            dto.setFileUri(uploadedFileRef.getFileUri());
            dto.setThumbnailUri(uploadedFileRef.getThumbnailUri());

            dto.setUploadStatus(FileUploadStatus.UPLOADED);
            dto.setStorageServiceId(fileMassStorageStrategy.getStorageServiceId());

            dto.setFileSize(fileSize);

            return entityService.saveUploadResult(dto);
        } catch (Exception e) {
            log.warn("Failed to upload attachment file.", e);

            if (file.exists()) {
                file.delete();
            }
            fileMassStorageStrategy.removeFileDirectory(dto.getId());
            entityService.markFileAsCorrupted(dto.getId());

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(dto.getFileName()));
        } finally {
            if (converted != null && converted.exists()) {
                converted.delete();
            }
        }
    }

    /** An attachment with any of these hashes (original or stored bytes), or null. */
    private AttachmentFileDTO findDuplicate(List<AttachmentFileHashDTO> hashes) {
        if (hashes == null) return null;
        for (AttachmentFileHashDTO hashDTO : hashes) {
            EntitySpecResult<AttachmentFile> entitySpecResult = EntitySpec.<AttachmentFile>specBuilder()
                    .where("fileHashes.hash", EntitySpec.specEquals(hashDTO.getHash()))
                    .build();

            Page<AttachmentFileDTO> page = entityService.getAllPaged(entitySpecResult.specString(), PageRequest.of(0, 1));
            if (page != null && !page.isEmpty()) {
                return page.getContent().getFirst();
            }
        }
        return null;
    }

    /**
     * Convert an image upload to JPEG XL: sets the stored format (image/jxl), remembers the
     * uploaded one, adds the hash of the JPEG XL next to the original's and returns the file to
     * store. Null (original stored) when the file isn't an image or the conversion didn't verify.
     */
    private File convertToJxl(AttachmentFileDTO dto, File file) {
        // a file replaced by a patch still carries the previous file's conversion fields
        dto.setOriginalMimeType(null);
        dto.setOriginalFileExtension(null);
        dto.setOriginalMetadata(null);
        if (!JxlConverter.isConvertible(dto.getMimeType())) {
            return null;
        }
        // read before converting: JPEG XL doesn't carry every format's metadata over
        var metadata = jxlConverter.metadataLostInConversion(file, dto.getMimeType());
        var conversion = jxlConverter.convert(file, dto.getMimeType());
        if (conversion.isEmpty()) {
            return null; // JxlConverter logged why; the upload is stored as it is
        }
        File jxl = conversion.get().jxlFile();
        dto.setOriginalMimeType(dto.getMimeType());
        dto.setOriginalFileExtension(dto.getFileExtension());
        dto.setMimeType(JxlConverter.JXL_MIME_TYPE);
        dto.setFileExtension(JxlConverter.JXL_EXTENSION);
        // what the JPEG XL can hold (as XMP) goes into it, before it is hashed; only the rest is kept here
        Map<String, Object> lost = metadata.<java.util.Map<String, Object>>map(LinkedHashMap::new).orElse(null);
        if (lost != null) {
            lost.keySet().removeAll(jxlConverter.embedMetadata(file, jxl, lost, dto.getOriginalMimeType()));
        }
        dto.setOriginalMetadata(lost == null || lost.isEmpty() ? null : lost);
        List<AttachmentFileHashDTO> hashes = new ArrayList<>(dto.getFileHashes() != null ? dto.getFileHashes() : List.of());
        for (AttachmentFileHashStrategy strategy : this.hashStrategies) {
            hashes.add(new AttachmentFileHashDTO(strategy.getHashType(), strategy.calculateHash(jxl), AttachmentFileHashTarget.STORED));
        }
        dto.setFileHashes(hashes);

        return jxl;
    }

    /** A linked duplicate is stored like the file it links to (format, original format, stored hashes). */
    private static void adoptStoredFormat(AttachmentFileDTO dto, AttachmentFileDTO existing) {
        dto.setMimeType(existing.getMimeType());
        dto.setFileExtension(existing.getFileExtension());
        dto.setOriginalMimeType(existing.getOriginalMimeType());
        dto.setOriginalFileExtension(existing.getOriginalFileExtension());
        dto.setOriginalMetadata(existing.getOriginalMetadata());
        dto.setFileSize(existing.getFileSize());
        List<AttachmentFileHashDTO> hashes = new ArrayList<>(dto.getFileHashes() != null ? dto.getFileHashes() : List.of());
        if (existing.getFileHashes() != null) {
            for (AttachmentFileHashDTO h : existing.getFileHashes()) {
                if (h.getTarget() == AttachmentFileHashTarget.STORED && hashes.stream().noneMatch(x -> x.getHash().equals(h.getHash()))) {
                    hashes.add(h);
                }
            }
        }
        dto.setFileHashes(hashes);
    }

    /** Undo {@link #adoptStoredFormat} when linking failed and the upload is stored on its own. */
    private static void restoreUploadedFormat(AttachmentFileDTO dto, AttachmentFileDTO existing) {
        if (existing.getOriginalMimeType() != null) {
            dto.setMimeType(existing.getOriginalMimeType());
            dto.setFileExtension(existing.getOriginalFileExtension());
        }
        dto.setOriginalMimeType(null);
        dto.setOriginalFileExtension(null);
        dto.setOriginalMetadata(null);
        dto.setFileSize(null);
        if (dto.getFileHashes() != null) {
            dto.setFileHashes(new ArrayList<>(dto.getFileHashes().stream().filter(h -> h.getTarget() != AttachmentFileHashTarget.STORED).toList()));
        }
    }

    private void generateFileHashes(AttachmentFileDTO dto, File file) {
        List<AttachmentFileHashDTO> attachmentFileHashDTOList = new ArrayList<>();

        for (AttachmentFileHashStrategy strategy : this.hashStrategies) {
            String hash = strategy.calculateHash(file);

            attachmentFileHashDTOList.add(new AttachmentFileHashDTO(strategy.getHashType(), hash));
        }

        dto.setFileHashes(attachmentFileHashDTOList);
    }

    private void extractFileMetadataToDto(AttachmentFileDTO dto, File file) throws IOException {
        String mimeType = tika.detect(file);

        dto.setMimeType(mimeType);
        dto.setFileSize(dto.getFileSize());

        dto.setFileExtension(fileExtension(dto.getFileName(), mimeType));
    }

    /**
     * The extension of the file name, or, when it has none (e.g. an image saved from a URL without
     * one), the usual extension of the detected type: "png" for image/png, "bin" when the type has none.
     */
    static String fileExtension(String fileName, String mimeType) {
        if (fileName != null) {
            int dotIndex = fileName.lastIndexOf('.');
            String fromName = dotIndex >= 0 ? fileName.substring(dotIndex + 1).trim() : "";
            if (!fromName.isEmpty() && fromName.length() <= 16 && fromName.chars().allMatch(Character::isLetterOrDigit)
                    && !contradictsImageType(fromName, mimeType)) {
                return fromName;
            }
        }
        try {
            String fromType = org.apache.tika.mime.MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
            if (fromType != null && fromType.length() > 1) {
                return fromType.substring(1); // ".png" -> "png"
            }
        } catch (Exception ignored) {
            // unknown or missing type: below
        }
        return "bin";
    }

    /**
     * An image whose name says another image format than its content (a GIF cover named "cover-1.jpg"): the
     * content wins. Only for images, whose type is read reliably from their first bytes; for other files
     * (text, documents) the name's extension stays, as the detected type is often only a generic one.
     */
    private static boolean contradictsImageType(String extension, String mimeType) {
        if (mimeType == null || !mimeType.startsWith("image/")) return false;
        try {
            List<String> extensions = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtensions();
            return !extensions.isEmpty() && extensions.stream().noneMatch(e -> e.equalsIgnoreCase("." + extension));
        } catch (Exception e) {
            return false;
        }
    }
}
