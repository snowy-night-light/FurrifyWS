package ws.furrify.core.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseEntityCrudService<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> {
    private final BaseEntityRepository<ENTITY> entityRepository;
    private final BaseDTOMapper<ENTITY, DTO> dtoMapper;

    @Transactional
    public Optional<DTO> findById(UUID id) {
        return entityRepository.findById(id, getCombinedSpecs()).map(dtoMapper::toDto);
    }

    @Transactional
    public Page<DTO> getAllPaged(Pageable pageable) {
        return entityRepository.findAll(pageable, getCombinedSpecs()).map(dtoMapper::toDto);
    }

    @Transactional
    public void deleteById(UUID id) {
        entityRepository.deleteById(id, getCombinedSpecs());
    }

    @Transactional
    public DTO partialUpdateById(UUID id, DTO patchDto) {
        ENTITY source = entityRepository.findById(id, getCombinedSpecs()).orElseThrow(() -> new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        dtoMapper.patchEntity(source, patchDto);

        return dtoMapper.toDto(
                this.entityRepository.save(source)
        );
    }

    @Transactional
    public DTO create(DTO dto) {
        return dtoMapper.toDto(
                entityRepository.save(
                        dtoMapper.toEntity(dto)
                )
        );
    }

    private Specification<ENTITY> getCombinedSpecs() {
        Specification<ENTITY> userScopedSpec;
        if (useUserScopeSpec()) {
            userScopedSpec = SecurityContextUtils.getUserScopedSecuritySpec();
        } else {
            userScopedSpec = Specification.unrestricted();
        }

        return customSecuritySpec()
                .and(userScopedSpec);
    }

    protected Specification<ENTITY> customSecuritySpec() {
        return Specification.unrestricted();
    }

    @SuppressWarnings("unchecked")
    protected Class<ENTITY> getEntityClass() {
        return (Class<ENTITY>) ResolvableType.forClass(getClass())
                .as(BaseEntityCrudService.class)
                .getGeneric(0)
                .resolve();
    }

    protected boolean useUserScopeSpec() {
        return UserScopedEntity.class.isAssignableFrom(getEntityClass());
    }
}
