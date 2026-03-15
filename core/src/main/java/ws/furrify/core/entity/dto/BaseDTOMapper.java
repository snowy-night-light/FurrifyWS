package ws.furrify.core.entity.dto;

import org.mapstruct.*;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.model.CycleAvoidingMappingContext;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@MapperConfig(
        componentModel = SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.FIELD
)
@Service
public interface BaseDTOMapper<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> {
    void patchDTO(@MappingTarget DTO source, DTO patchDto, @Context CycleAvoidingMappingContext context);

    ENTITY toEntity(DTO dto, @Context CycleAvoidingMappingContext context);

    DTO toDto(ENTITY entity, @Context CycleAvoidingMappingContext context);

    List<ENTITY> toEntityList(List<DTO> dtoList, @Context CycleAvoidingMappingContext context);

    List<DTO> toDtoList(List<ENTITY> entityList, @Context CycleAvoidingMappingContext context);
}