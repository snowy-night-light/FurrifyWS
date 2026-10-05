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
package ws.furrify.storage.dto.tag.alias;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;
import ws.furrify.storage.dto.tag.category.TagCategoryDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {TagCategoryDTOMapper.class}
)
public interface TagAliasDTOMapper extends BaseDTOMapper<TagAlias, TagAliasDTO, PatchTagAliasRequest> {

    @Override
    @Mapping(target = "targetTag", qualifiedByName = "tagToTagDtoWithoutAliases")
    TagAliasDTO toDto(TagAlias entity, @Context CycleAvoidingMappingContext context);

    @Named("tagToTagDtoWithoutAliases")
    @Mapping(target = "aliases", ignore = true)
    TagDTO tagToTagDtoWithoutAliases(Tag tag, @Context CycleAvoidingMappingContext context);

    @Named("tagAliasToTagAliasDtoWithoutTargetTag")
    @Mapping(target = "targetTag", ignore = true)
    TagAliasDTO tagAliasToTagAliasDtoWithoutTargetTag(TagAlias entity, @Context CycleAvoidingMappingContext context);
}