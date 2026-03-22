package ws.furrify.core.entity.dto;

import org.mapstruct.*;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.core.utils.HibernateLazyLoaderMappingChecker;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@MapperConfig(
        componentModel = SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.FIELD,
        uses = HibernateLazyLoaderMappingChecker.class
)
@Service
public interface BaseDTOMapper<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> {
    @Named("patchEntityDefault")
    default void patchEntity(ENTITY source, DTO patchDto) {
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

    void patchEntity(@MappingTarget ENTITY source, DTO patchDto, @Context CycleAvoidingMappingContext context);

    ENTITY toEntity(DTO dto, @Context CycleAvoidingMappingContext context);

    DTO toDto(ENTITY entity, @Context CycleAvoidingMappingContext context);

    List<ENTITY> toEntityList(List<DTO> dtoList, @Context CycleAvoidingMappingContext context);

    List<DTO> toDtoList(List<ENTITY> entityList, @Context CycleAvoidingMappingContext context);
}