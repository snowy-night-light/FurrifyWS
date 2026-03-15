package ws.furrify.core.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.core.entity.UserScopedEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.dto.UserScopedEntityDTO;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.UserNotAuthenticatedException;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public abstract class UserScopedEntityCrudService<ENTITY extends UserScopedEntity, DTO extends UserScopedEntityDTO<ENTITY>> extends BaseEntityCrudService<ENTITY, DTO> {
    private final UserScopedEntityRepository<ENTITY> entityRepository;
    private final BaseDTOMapper<ENTITY, DTO> dtoMapper;

    public UserScopedEntityCrudService(UserScopedEntityRepository<ENTITY> entityRepository, BaseDTOMapper<ENTITY, DTO> dtoMapper) {
        super(entityRepository, dtoMapper);
        this.entityRepository = entityRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public Optional<DTO> findById(UUID id) {
        return entityRepository.findByIdAndOwnerId(id, this.getCurrentUserId()).map(dtoMapper::toDto);
    }

    public Page<DTO> getAllPaged(Pageable pageable) {
        return entityRepository.findAllByOwnerId(this.getCurrentUserId(), pageable).map(dtoMapper::toDto);
    }

    public void deleteById(UUID id) {
        entityRepository.deleteByIdAndOwnerId(id, this.getCurrentUserId());
    }

    public DTO partialUpdateById(UUID id, DTO patchDto) {
        DTO sourceDTO = entityRepository.findByIdAndOwnerId(id, this.getCurrentUserId()).map(dtoMapper::toDto).orElseThrow(() -> new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        dtoMapper.patchDTO(sourceDTO, patchDto);

        return this.save(sourceDTO);
    }

    public DTO create(DTO dto) {
        return this.save(dto);
    }

    private UUID getCurrentUserId() {
        Optional<UUID> ownerId = SecurityContextUtils.getCurrentSubject();
        if (ownerId.isEmpty()) {
            log.error("Invalid state, user was not authenticated at controller level but is demanding user scoped change.");

            throw new UserNotAuthenticatedException(Errors.USER_NOT_AUTHENTICATED.getErrorMessage());
        }

        return ownerId.get();
    }
}
