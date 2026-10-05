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
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.request.PatchTagRequest;
import ws.furrify.storage.service.library.LibraryEntityService;

import java.util.Map;
import java.util.UUID;

@Service
public class TagEntityService extends BaseEntityCrudService<Tag, TagDTO, PatchTagRequest> {

    private final TagCategoryEntityService tagCategoryEntityService;
    private final LibraryEntityService libraryEntityService;

    @Autowired
    public TagEntityService(BaseEntityRepository<Tag> entityRepository, BaseDTOMapper<Tag, TagDTO, PatchTagRequest> dtoMapper, TagCategoryEntityService tagCategoryEntityService, LibraryEntityService libraryEntityService) {
        super(entityRepository, dtoMapper);
        this.tagCategoryEntityService = tagCategoryEntityService;
        this.libraryEntityService = libraryEntityService;
    }

    @Override
    protected TagDTO handleCreate(TagDTO dto) {
        super.handleInternalReference(dto, TagDTO::getCategory, TagDTO::setCategory, tagCategoryEntityService);
        super.handleInternalReference(dto, TagDTO::getLibrary, TagDTO::setLibrary, libraryEntityService);

        super.handleUniqueConstraint(dto, Map.of(
                "name", TagDTO::getName
        ));

        return super.handleCreate(dto);
    }

    @Override
    protected TagDTO handlePatch(UUID id, PatchTagRequest patchDto) {
        super.handleInternalReference(patchDto.getCategory(), tagCategoryEntityService);
        super.handleInternalReference(patchDto.getLibrary(), libraryEntityService);

        super.handleUniqueConstraint(id, patchDto, Map.of(
                "name", PatchTagRequest::getName
        ));

        return super.handlePatch(id, patchDto);
    }
}
