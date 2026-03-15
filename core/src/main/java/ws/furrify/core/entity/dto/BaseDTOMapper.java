package ws.furrify.core.entity.dto;

import org.mapstruct.*;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntity;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@MapperConfig(
        componentModel = SPRING,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG,
        builder = @Builder(disableBuilder = true)
)
@Service
public interface BaseDTOMapper<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> {
    void patchDTO(@MappingTarget DTO source, DTO patchDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ENTITY toEntity(DTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DTO toDto(ENTITY entity);
}