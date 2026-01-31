package ws.furrify.core.entity.dto;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntity;

@Service
public interface BaseEntityDTOMapper<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequestDTO<ENTITY, DTO>> {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(PATCH_REQ patchDto,  @MappingTarget ENTITY entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ENTITY toEntity(DTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DTO toDto(ENTITY entity);
}