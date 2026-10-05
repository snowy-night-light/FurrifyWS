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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;

import java.util.Map;
import java.util.UUID;

@Service
public class TagAliasEntityService extends BaseEntityCrudService<TagAlias, TagAliasDTO, PatchTagAliasRequest> {

    private final TagEntityService tagEntityService;

    @Autowired
    public TagAliasEntityService(BaseEntityRepository<TagAlias> entityRepository, BaseDTOMapper<TagAlias, TagAliasDTO, PatchTagAliasRequest> dtoMapper, @Lazy TagEntityService tagEntityService) {
        super(entityRepository, dtoMapper);
        this.tagEntityService = tagEntityService;
    }

    @Override
    protected TagAliasDTO handleCreate(TagAliasDTO dto) {
        super.handleInternalReference(dto, TagAliasDTO::getTargetTag, TagAliasDTO::setTargetTag, tagEntityService);

        super.handleUniqueConstraint(dto, Map.of(
                "alias", TagAliasDTO::getAlias
        ));

        return super.handleCreate(dto);
    }

    @Override
    protected TagAliasDTO handlePatch(UUID id, PatchTagAliasRequest patchDto) {
        super.handleInternalReference(patchDto.getTargetTag(), tagEntityService);

        super.handleUniqueConstraint(id, patchDto, Map.of(
                "alias", PatchTagAliasRequest::getAlias
        ));

        return super.handlePatch(id, patchDto);
    }
}
