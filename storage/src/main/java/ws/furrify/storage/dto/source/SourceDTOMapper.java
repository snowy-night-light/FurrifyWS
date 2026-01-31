package ws.furrify.storage.dto.source;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.tag.PatchTagRequestDTO;
import ws.furrify.storage.dto.tag.TagDTO;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface SourceDTOMapper extends BaseEntityDTOMapper<Source, SourceDTO, PatchSourceRequestDTO> {
}