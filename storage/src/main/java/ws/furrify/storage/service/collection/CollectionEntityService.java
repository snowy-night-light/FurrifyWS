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
package ws.furrify.storage.service.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.dto.collection.CollectionDTO;
import ws.furrify.storage.dto.collection.request.PatchCollectionRequest;
import ws.furrify.storage.service.library.LibraryEntityService;
import ws.furrify.storage.service.post.PostEntityService;

import java.util.UUID;

@Service
public class CollectionEntityService extends BaseEntityCrudService<Collection, CollectionDTO, PatchCollectionRequest> {

    private final PostEntityService postEntityService;
    private final LibraryEntityService libraryEntityService;

    @Autowired
    public CollectionEntityService(BaseEntityRepository<Collection> entityRepository, BaseDTOMapper<Collection, CollectionDTO, PatchCollectionRequest> dtoMapper, PostEntityService postEntityService, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.postEntityService = postEntityService;
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    protected CollectionDTO handlePatch(UUID id, PatchCollectionRequest patchDto) {
        super.handleCollectionInternalReferences(patchDto.getPosts(), postEntityService);
        super.handleInternalReference(patchDto.getLibrary(), libraryEntityService);

        return super.handlePatch(id, patchDto);
    }

    @Override
    protected CollectionDTO handleCreate(CollectionDTO dto) {
        super.handleInternalCollectionReferences(dto, CollectionDTO::getPosts, CollectionDTO::setPosts, postEntityService);
        super.handleInternalReference(dto, CollectionDTO::getLibrary, CollectionDTO::setLibrary, libraryEntityService);

        return super.handleCreate(dto);
    }
}
