package ws.furrify.storage.dto.file;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.PatchArtistRequestDTO;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface FileDTOMapper extends BaseEntityDTOMapper<File, FileDTO, PatchFileRequestDTO> {
}