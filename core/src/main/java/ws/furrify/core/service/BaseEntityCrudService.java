package ws.furrify.core.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseEntityCrudService<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> {
    private final BaseEntityRepository<ENTITY> entityRepository;
    private final BaseDTOMapper<ENTITY, DTO> dtoMapper;

    public Optional<DTO> findById(UUID id) {
        return entityRepository.findById(id).map(dtoMapper::toDto);
    }

    public Page<DTO> getAllPaged(Pageable pageable) {
        return entityRepository.findAll(pageable).map(dtoMapper::toDto);
    }

    public void deleteById(UUID id) {
        entityRepository.deleteById(id);
    }

    public DTO partialUpdateById(UUID id, DTO patchDto) {
        DTO sourceDTO = entityRepository.findById(id).map(dtoMapper::toDto).orElseThrow(() -> new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        dtoMapper.patchDTO(sourceDTO, patchDto);

        return this.save(sourceDTO);
    }

    public DTO create(DTO dto) {
        return this.save(dto);
    }

    protected DTO save(DTO dto) {
        return dtoMapper.toDto(
                entityRepository.save(dtoMapper.toEntity(dto))
        );
    }
}
