package ws.furrify.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
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
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.exception.UniqueConstraintViolationException;
import ws.furrify.core.specification.EntitySpec;
import ws.furrify.core.specification.EntitySpecResult;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static ws.furrify.core.specification.EntitySpec.specEquals;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseEntityCrudService<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> {
    protected final BaseEntityRepository<ENTITY> entityRepository;
    protected final BaseDTOMapper<ENTITY, DTO, PATCH_REQ> dtoMapper;

    @Transactional
    public Optional<DTO> findById(UUID id) {
        return entityRepository.findById(id, getCombinedSpecs()).map(dtoMapper::toDto);
    }

    @Transactional
    public boolean existsById(UUID id) {
        return entityRepository.existsById(id, getCombinedSpecs());
    }

    @Transactional
    public Page<DTO> getAllPaged(String spec, Pageable pageable) {
        return entityRepository.findAll(pageable, getCombinedSpecs(
                EntitySpec.fromSpecString(spec)
        )).map(dtoMapper::toDto);
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
    protected DTO internalPutById(UUID id, DTO dto) {
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

    public <REF_ENTITY extends BaseEntity, REF_DTO extends BaseEntityDTO<REF_ENTITY>, REF_PATCH_REQ extends BasePatchEntityRequest<REF_ENTITY, REF_DTO>>
    void handleInternalReference(DTO dto,
                                 Function<DTO, REF_DTO> getter,
                                 BiConsumer<DTO, REF_DTO> setter,
                                 BaseEntityCrudService<REF_ENTITY, REF_DTO, REF_PATCH_REQ> referenceEntityService) {
        REF_DTO refEntity = getter.apply(dto);

        if (refEntity != null) {
            setter.accept(
                    dto,
                    referenceEntityService.findById(refEntity.getId()).orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(refEntity.getId())))
            );
        }
    }

    public <VALUE> void handleUniqueConstraint(DTO dto, Map<String, Function<DTO, VALUE>> values) {
        handleUniqueConstraintInternal(dto, values, null);
    }

    public <VALUE> void handleUniqueConstraint(UUID id, PATCH_REQ patchReq, Map<String, Function<PATCH_REQ, VALUE>> values) {
        handleUniqueConstraintInternal(patchReq, values, id);
    }

    private <T, VALUE> void handleUniqueConstraintInternal(T source, Map<String, Function<T, VALUE>> values, UUID currentRecordId) {
        List<EntitySpecResult<ENTITY>> specs = new ArrayList<>();

        values.forEach((field, getter) -> {
            Object val = getter.apply(source);
            if (val instanceof JsonNullable<?> jsonNullable) {
                if (!jsonNullable.isPresent()) return;
                val = jsonNullable.orElse(null);
            }
            
            specs.add(
                    EntitySpec.<ENTITY>specBuilder()
                            .where(field, specEquals(val))
                            .build()
            );
        });

        if (specs.isEmpty()) return;
        
        EntitySpecResult<ENTITY> combinedSpec = EntitySpec.specCombineAllWithAnd(specs);
        
        if (currentRecordId != null) {
            combinedSpec = EntitySpec.<ENTITY>specBuilder()
                    .where("id", EntitySpec.specNotEquals(currentRecordId))
                    .and(combinedSpec)
                    .build();
        }

        long totalCount = entityRepository.count(combinedSpec);
        if (totalCount > 0) {
            throw new UniqueConstraintViolationException(Errors.UNIQUE_CONSTRAINT_VIOLATION.getErrorMessage(String.join(",", values.keySet())));
        }
    }

    public <REF_ENTITY extends BaseEntity, REF_DTO extends BaseEntityDTO<REF_ENTITY>, REF_PATCH_REQ extends BasePatchEntityRequest<REF_ENTITY, REF_DTO>>
    void handleInternalCollectionReferences(DTO dto,
                                            Function<DTO, List<REF_DTO>> getter,
                                            BiConsumer<DTO, List<REF_DTO>> setter,
                                            BaseEntityCrudService<REF_ENTITY, REF_DTO, REF_PATCH_REQ> referenceEntityService) {
        List<REF_DTO> refEntities = getter.apply(dto);

        if (refEntities != null) {
            setter.accept(
                    dto,
                    refEntities.stream()
                            .map(refEntity -> referenceEntityService.findById(refEntity.getId())
                                    .orElseThrow(() -> new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(refEntity.getId()))))
                            .toList()
            );
        }
    }

    public <REF_ENTITY extends BaseEntity, REF_DTO extends BaseEntityDTO<REF_ENTITY>, REF_PATCH_REQ extends BasePatchEntityRequest<REF_ENTITY, REF_DTO>>
    void handleInternalReference(JsonNullable<EntityIdRequest> entityIdRequest, BaseEntityCrudService<REF_ENTITY, REF_DTO, REF_PATCH_REQ> referenceEntityService) {
        if (entityIdRequest.isPresent()) {
            if (!referenceEntityService.existsById(entityIdRequest.get().getId())) {
                throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.get().getId()));
            }
        }
    }

    public <REF_ENTITY extends BaseEntity, REF_DTO extends BaseEntityDTO<REF_ENTITY>, REF_PATCH_REQ extends BasePatchEntityRequest<REF_ENTITY, REF_DTO>>
    void handleCollectionInternalReferences(JsonNullable<List<EntityIdRequest>> entityIdRequests, BaseEntityCrudService<REF_ENTITY, REF_DTO, REF_PATCH_REQ> referenceEntityService) {
        if (entityIdRequests.isPresent()) {
            for (EntityIdRequest entityIdRequest : entityIdRequests.get()) {
                if (!referenceEntityService.existsById(entityIdRequest.getId())) {
                    throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(entityIdRequest.getId()));
                }
            }

        }
    }

    private EntitySpecResult<ENTITY> getCombinedSpecs() {
        return getCombinedSpecs(null);
    }

    private EntitySpecResult<ENTITY> getCombinedSpecs(EntitySpecResult<ENTITY> additionalSpec) {
        EntitySpecResult<ENTITY> userScopedSpec;
        if (useUserScopeSpec()) {
            userScopedSpec = SecurityContextUtils.getUserScopedSecuritySpec();
        } else {
            userScopedSpec = EntitySpec.unrestricted();
        }

        return EntitySpec.from(customSecuritySpec()).and(userScopedSpec).and(additionalSpec).build();
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
