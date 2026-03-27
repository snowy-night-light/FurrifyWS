package ws.furrify.core.entity.request;

import org.mapstruct.*;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.mappers.JsonNullableMapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@MapperConfig(
        componentModel = SPRING,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {JsonNullableMapper.class}
)
@Service
public interface BaseRequestMapper<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, CREATE_REQ extends BaseCreateEntityRequest<ENTITY, DTO>> {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DTO toDto(CREATE_REQ createRequest);
}