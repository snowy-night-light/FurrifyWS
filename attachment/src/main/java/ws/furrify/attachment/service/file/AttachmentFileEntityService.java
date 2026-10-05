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
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.domain.file.strategy.hash.AttachmentFileHashStrategy;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;
import ws.furrify.attachment.dto.file.vo.AttachmentFileHashDTO;
import ws.furrify.attachment.exception.AttachmentErrors;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.service.file.storage.vo.UploadedFileReference;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.service.BaseEntityCrudService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;


@Service
@Slf4j
public class AttachmentFileEntityService extends BaseEntityCrudService<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> {

    private final Set<AttachmentFileHashStrategy> hashStrategies;
    private final FileMassStorageStrategy fileMassStorageStrategy;
    private final Tika tika;

    @Autowired
    public AttachmentFileEntityService(BaseEntityRepository<AttachmentFile> entityRepository, BaseDTOMapper<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> dtoMapper, Set<AttachmentFileHashStrategy> hashStrategies, FileMassStorageStrategy fileMassStorageStrategy, Tika tika) {
        super(entityRepository, dtoMapper);
        this.hashStrategies = hashStrategies;
        this.fileMassStorageStrategy = fileMassStorageStrategy;
        this.tika = tika;
    }

    @Override
    protected java.util.Optional<AttachmentFileDTO> handleDelete(UUID id) {
        java.util.Optional<AttachmentFileDTO> result = super.handleDelete(id);

        fileMassStorageStrategy.removeFileDirectory(id);
        
        return result;
    }

    @Transactional
    public AttachmentFileDTO createWithFileUpload(AttachmentFileDTO dto, MultipartFile multipartFile) {

        dto.setUploadStatus(FileUploadStatus.NOT_UPLOADED);

        File tempFile = null;
        try {
            tempFile = Files.createTempFile("upload-", multipartFile.getOriginalFilename()).toFile();
            multipartFile.transferTo(tempFile);

            extractFileMetadataToDto(dto, tempFile);
            generateFileHashes(dto, tempFile);

            AttachmentFileDTO createdDto = super.create(dto);
            return uploadFile(createdDto, tempFile, false);
        } catch (Exception e) {
            log.warn("Failed to process attachment file uploaded data.", e);

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(dto.getFileName()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Transactional
    public AttachmentFileDTO patchWithFileUpload(UUID id, PatchAttachmentFileRequest patchDto, @NotNull MultipartFile multipartFile) {
        AttachmentFileDTO patchedDto = super.handlePatch(id, patchDto);

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void markFileAsCorrupted(UUID id) {
        AttachmentFileDTO dto = internalFindById(id).orElse(null);
        if (dto == null) {
            return;
        }

        dto.setUploadStatus(FileUploadStatus.CORRUPTED);

        internalPutById(id, dto);
    }

    @Transactional
    protected AttachmentFileDTO uploadFile(AttachmentFileDTO dto, File file, boolean replaceExisting) {
        try {
            UploadedFileReference uploadedFileRef = null;

            // Check if file with same hash exists
            AttachmentFileDTO existingFile = null;
            if (dto.getFileHashes() != null && !dto.getFileHashes().isEmpty()) {
                for (AttachmentFileHashDTO hashDTO : dto.getFileHashes()) {
                    EntitySpecResult<AttachmentFile> entitySpecResult = EntitySpec.<AttachmentFile>specBuilder()
                            .where("fileHashes.hash", EntitySpec.specEquals(hashDTO.getHash()))
                            .build();

                    Page<AttachmentFileDTO> page = this.getAllPaged(entitySpecResult.specString(), PageRequest.of(0, 1));
                    if (!page.isEmpty()) {
                        existingFile = page.getContent().get(0);
                        break;
                    }
                }
            }

            if (existingFile != null && existingFile.getFileUri() != null) {
                try {
                    uploadedFileRef = fileMassStorageStrategy.linkFile(dto.getId(), dto.getMimeType(), existingFile.getFileUri(), existingFile.getThumbnailUri());
                    file.delete();
                } catch (Exception linkException) {
                    log.warn("Failed to link existing file {}. Falling back to normal upload. Reason: {}", existingFile.getId(), linkException.getMessage());
                    fileMassStorageStrategy.removeFileDirectory(dto.getId());
                    uploadedFileRef = fileMassStorageStrategy.uploadFile(dto.getId(), dto.getMimeType(), file, replaceExisting);
                }
            } else {
                uploadedFileRef = fileMassStorageStrategy.uploadFile(dto.getId(), dto.getMimeType(), file, replaceExisting);
            }

            dto.setFileUri(uploadedFileRef.getFileUri());
            dto.setThumbnailUri(uploadedFileRef.getThumbnailUri());

            dto.setUploadStatus(FileUploadStatus.UPLOADED);
            dto.setStorageServiceId(fileMassStorageStrategy.getStorageServiceId());

            dto.setFileSize(file.length());

            return internalPutById(dto.getId(), dto);
        } catch (Exception e) {
            log.warn("Failed to upload attachment file.", e);

            if (file.exists()) {
                file.delete();
            }
            fileMassStorageStrategy.removeFileDirectory(dto.getId());
            markFileAsCorrupted(dto.getId());

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(dto.getFileName()));
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

        int dotIndex = dto.getFileName().lastIndexOf(".");
        if (dotIndex >= 0) {
            dto.setFileExtension(dto.getFileName().substring(dotIndex + 1));
        }
    }

}
