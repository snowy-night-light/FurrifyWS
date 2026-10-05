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
package ws.furrify.storage.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;
import ws.furrify.storage.dto.tag.category.request.PatchTagCategoryRequest;
import ws.furrify.storage.service.library.LibraryEntityService;

import java.util.Map;
import java.util.UUID;

@Service
public class TagCategoryEntityService extends BaseEntityCrudService<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> {

    private final LibraryEntityService libraryEntityService;

    @Autowired
    public TagCategoryEntityService(BaseEntityRepository<TagCategory> entityRepository, BaseDTOMapper<TagCategory, TagCategoryDTO, PatchTagCategoryRequest> dtoMapper, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    protected TagCategoryDTO handlePatch(UUID id, PatchTagCategoryRequest patchDto) {
        this.handleInternalReference(patchDto.getLibrary(), libraryEntityService);

        super.handleUniqueConstraint(id, patchDto, Map.of(
                "name", PatchTagCategoryRequest::getName
        ));

        return super.handlePatch(id, patchDto);
    }

    @Override
    protected TagCategoryDTO handleCreate(TagCategoryDTO dto) {
        this.handleInternalReference(dto, TagCategoryDTO::getLibrary, TagCategoryDTO::setLibrary, libraryEntityService);

        super.handleUniqueConstraint(dto, Map.of(
                "name", TagCategoryDTO::getName
        ));

        return super.handleCreate(dto);
    }
}
