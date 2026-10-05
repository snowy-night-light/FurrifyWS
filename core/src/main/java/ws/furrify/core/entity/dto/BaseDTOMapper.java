/*
 * furrify-core - Furrify Workspace Project
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
package ws.furrify.core.entity.dto;

import org.mapstruct.*;
import org.openapitools.jackson.nullable.JsonNullable;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.mappers.EntityReferenceMapper;
import ws.furrify.core.mappers.HibernateLazyLoaderMappingChecker;
import ws.furrify.core.mappers.JsonNullableMapper;
import ws.furrify.core.mappers.MapMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@MapperConfig(
        componentModel = SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.FIELD,
        uses = {JsonNullableMapper.class, HibernateLazyLoaderMappingChecker.class, EntityReferenceMapper.class, MapMapper.class}
)
public interface BaseDTOMapper<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, PATCH_DTO extends BasePatchEntityRequest<ENTITY, DTO>> {

    @Named("patchEntityDefault")
    default void putEntity(ENTITY source, DTO putDto) {
        putEntity(source, putDto, new CycleAvoidingMappingContext());
    }

    @Named("putEntityDefault")
    default void patchEntity(ENTITY source, PATCH_DTO patchDto) {
        patchEntity(source, patchDto, new CycleAvoidingMappingContext());
    }

    @Named("toEntityDefault")
    default ENTITY toEntity(DTO dto) {
        return toEntity(dto, new CycleAvoidingMappingContext());
    }

    @Named("toDtoDefault")
    default DTO toDto(ENTITY entity) {
        return toDto(entity, new CycleAvoidingMappingContext());
    }

    @Named("toEntityListDefault")
    default List<ENTITY> toEntityList(List<DTO> dtoList) {
        return toEntityList(dtoList, new CycleAvoidingMappingContext());
    }

    @Named("toDtoListDefault")
    default List<DTO> toDtoList(List<ENTITY> entityList) {
        return toDtoList(entityList, new CycleAvoidingMappingContext());
    }

    void putEntity(@MappingTarget ENTITY source, DTO putDto, @Context CycleAvoidingMappingContext context);

    void patchEntity(@MappingTarget ENTITY source, PATCH_DTO patchDto, @Context CycleAvoidingMappingContext context);

    ENTITY toEntity(DTO dto, @Context CycleAvoidingMappingContext context);

    DTO toDto(ENTITY entity, @Context CycleAvoidingMappingContext context);

    List<ENTITY> toEntityList(List<DTO> dtoList, @Context CycleAvoidingMappingContext context);

    List<DTO> toDtoList(List<ENTITY> entityList, @Context CycleAvoidingMappingContext context);

    ENTITY mapIdToEntity(EntityIdRequest request);

    default ENTITY updateEntityId(EntityIdRequest request, @MappingTarget ENTITY existingTarget) {
        if (request == null) {
            return null;
        }
        return mapIdToEntity(request);
    }

    default List<ENTITY> mapJsonNullableIdList(JsonNullable<List<EntityIdRequest>> value) {
        if (value == null || !value.isPresent() || value.get() == null) {
            return null;
        }
        return value.get().stream().map(this::mapIdToEntity).toList();
    }
}
