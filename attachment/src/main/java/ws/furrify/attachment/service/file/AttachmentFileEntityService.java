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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.FileUploadStatus;
import ws.furrify.attachment.dto.file.AttachmentFileDTO;
import ws.furrify.attachment.dto.file.request.PatchAttachmentFileRequest;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AttachmentFileEntityService extends BaseEntityCrudService<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> {

    private final FileMassStorageStrategy fileMassStorageStrategy;

    /**
     * The records of attachment files. Uploads are done by {@link AttachmentFileProcessorService}, which calls
     * the transactional methods here one by one (through this bean's proxy), so no transaction is open
     * while the file itself is processed.
     */
    @Autowired
    public AttachmentFileEntityService(BaseEntityRepository<AttachmentFile> entityRepository,
                                       BaseDTOMapper<AttachmentFile, AttachmentFileDTO, PatchAttachmentFileRequest> dtoMapper,
                                       FileMassStorageStrategy fileMassStorageStrategy) {
        super(entityRepository, dtoMapper);
        this.fileMassStorageStrategy = fileMassStorageStrategy;
    }

    @Override
    protected Optional<AttachmentFileDTO> handleDelete(UUID id) {
        Optional<AttachmentFileDTO> result = super.handleDelete(id);

        fileMassStorageStrategy.removeFileDirectory(id);
        
        return result;
    }

    /** The patched record, not saved yet: the upload saves it with the file's details. */
    @Transactional(readOnly = true)
    public AttachmentFileDTO applyPatch(UUID id, PatchAttachmentFileRequest patchDto) {
        return handlePatch(id, patchDto);
    }

    /** Save the record with the details of its stored file. */
    @Transactional
    public AttachmentFileDTO saveUploadResult(AttachmentFileDTO dto) {
        return internalPutById(dto.getId(), dto);
    }

    @Transactional
    public void markFileAsCorrupted(UUID id) {
        AttachmentFileDTO dto = internalFindById(id).orElse(null);
        if (dto == null) {
            return;
        }
        dto.setUploadStatus(FileUploadStatus.CORRUPTED);
        internalPutById(id, dto);
    }
}
