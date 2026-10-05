/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.service.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;

import java.util.UUID;

@Service
public class LibraryEntityService extends BaseEntityCrudService<Library, LibraryDTO, PatchLibraryRequest> {

    @Autowired
    public LibraryEntityService(BaseEntityRepository<Library> entityRepository, BaseDTOMapper<Library, LibraryDTO, PatchLibraryRequest> dtoMapper) {
        super(entityRepository, dtoMapper);
    }

    @Override
    protected LibraryDTO handlePatch(UUID id, PatchLibraryRequest patchDto) {
        return super.handlePatch(id, patchDto);
    }

    @Override
    protected LibraryDTO handleCreate(LibraryDTO dto) {
        return super.handleCreate(dto);
    }
}
