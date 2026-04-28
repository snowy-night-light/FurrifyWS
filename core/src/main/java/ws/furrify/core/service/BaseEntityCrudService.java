package ws.furrify.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseEntityCrudService<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> {
    private final BaseEntityRepository<ENTITY> entityRepository;
    private final BaseDTOMapper<ENTITY, DTO, PATCH_REQ> dtoMapper;

    @Transactional
    public Optional<DTO> findById(UUID id) {
        return entityRepository.findById(id, getCombinedSpecs()).map(dtoMapper::toDto);
    }

    @Transactional
    public boolean existsById(UUID id) {
        return entityRepository.existsById(id, getCombinedSpecs());
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
    public DTO patchById(UUID id, PATCH_REQ patchDto) {
        ENTITY source = entityRepository.findById(id, getCombinedSpecs()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        dtoMapper.patchEntity(source, patchDto);

        return dtoMapper.toDto(
                this.entityRepository.save(source)
        );
    }

    @Transactional
    protected DTO putById(UUID id, DTO dto) {
        ENTITY source = entityRepository.findById(id, getCombinedSpecs()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        dtoMapper.putEntity(source, dto);

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

    private EntitySpecResult<ENTITY> getCombinedSpecs() {
        EntitySpecResult<ENTITY> userScopedSpec;
        if (useUserScopeSpec()) {
            userScopedSpec = SecurityContextUtils.getUserScopedSecuritySpec();
        } else {
            userScopedSpec = EntitySpec.unrestricted();
        }

        return EntitySpec.from(customSecuritySpec()).and(userScopedSpec).build();
    }

    protected EntitySpecResult<ENTITY> customSecuritySpec() {
        return EntitySpec.unrestricted();
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
