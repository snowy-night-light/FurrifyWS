package ws.furrify.attachment.service.file;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
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
    public void deleteById(UUID id) {
        super.deleteById(id);

        fileMassStorageStrategy.removeFileDirectory(id);
    }

    @Transactional
    public AttachmentFileDTO createWithFileUpload(AttachmentFileDTO dto, MultipartFile multipartFile) {

        dto.setUploadStatus(FileUploadStatus.NOT_UPLOADED);

        try {
            File tempFile = Files.createTempFile("upload-", multipartFile.getOriginalFilename()).toFile();
            multipartFile.transferTo(tempFile);

            extractFileMetadataToDto(dto, tempFile);
            generateFileHashes(dto, tempFile);

            AttachmentFileDTO createdDto = super.create(dto);
            uploadFile(createdDto, tempFile, false);

            return createdDto;
        } catch (Exception e) {
            log.warn("Failed to process attachment file uploaded data.", e);

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(dto.getFileName()));
        }


    }

    @Transactional
    public AttachmentFileDTO patchWithFileUpload(UUID id, PatchAttachmentFileRequest patchDto, @NotNull MultipartFile multipartFile) {
        AttachmentFileDTO patchedDto = super.patchById(id, patchDto);

        try {
            File tempFile = Files.createTempFile("upload-", multipartFile.getOriginalFilename()).toFile();
            multipartFile.transferTo(tempFile);

            extractFileMetadataToDto(patchedDto, tempFile);
            generateFileHashes(patchedDto, tempFile);

            uploadFile(patchedDto, tempFile, true);

            return patchedDto;
        } catch (Exception e) {
            log.warn("Failed to process attachment file uploaded data.", e);

            throw new ServiceLogicException(AttachmentErrors.FILE_PROCESSING_FAILURE.getErrorMessage(patchedDto.getFileName()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void markFileAsCorrupted(UUID id) {
        AttachmentFileDTO dto = findById(id).orElse(null);
        if (dto == null) {
            return;
        }

        dto.setUploadStatus(FileUploadStatus.CORRUPTED);

        putById(id, dto);
    }

    @Transactional
    protected void uploadFile(AttachmentFileDTO dto, File file, boolean replaceExisting) {
        try {
            UploadedFileReference uploadedFileRef = fileMassStorageStrategy.uploadFile(dto.getId(), dto.getMimeType(), file, replaceExisting);
            dto.setFileUri(uploadedFileRef.getFileUri());
            dto.setThumbnailUri(uploadedFileRef.getThumbnailUri());

            dto.setUploadStatus(FileUploadStatus.UPLOADED);
            dto.setStorageServiceId(fileMassStorageStrategy.getStorageServiceId());

            dto.setFileSize(file.length());

            putById(dto.getId(), dto);
        } catch (Exception e) {
            log.warn("Failed to upload attachment file.", e);

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

        dto.setMimeType(MimeType.valueOf(mimeType));
        dto.setFileSize(dto.getFileSize());
    }

}
