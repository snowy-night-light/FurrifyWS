package ws.furrify.core.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseEntityCrudService<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>> {
    private final BaseEntityRepository<ENTITY> entityRepository;
    private final BaseDTOMapper<ENTITY, DTO> dtoMapper;

    public Optional<DTO> findById(UUID id) {
        return entityRepository.findById(id, getCombinedSpecs()).map(ent -> dtoMapper.toDto(ent, new CycleAvoidingMappingContext()));
    }

    public Page<DTO> getAllPaged(Pageable pageable) {
        return entityRepository.findAll(pageable, getCombinedSpecs()).map(ent -> dtoMapper.toDto(ent, new CycleAvoidingMappingContext()));
    }

    public void deleteById(UUID id) {
        entityRepository.deleteById(id, getCombinedSpecs());
    }

    public DTO partialUpdateById(UUID id, DTO patchDto) {
        DTO sourceDTO = entityRepository.findById(id, getCombinedSpecs()).map(ent -> dtoMapper.toDto(ent, new CycleAvoidingMappingContext())).orElseThrow(() -> new EntityNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id)));

        dtoMapper.patchDTO(sourceDTO, patchDto, new CycleAvoidingMappingContext());

        return this.save(sourceDTO);
    }

    public DTO create(DTO dto) {
        return this.save(dto);
    }

    protected DTO save(DTO dto) {
        CycleAvoidingMappingContext mappingContext = new CycleAvoidingMappingContext();

        return dtoMapper.toDto(
                entityRepository.save(dtoMapper.toEntity(dto, mappingContext)),
                mappingContext
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
        ResolvableType type = ResolvableType.forClass(getClass()).as(BaseEntity.class);

        return (Class<ENTITY>) type.getGeneric(1).resolve();
    }

    protected boolean useUserScopeSpec() {
        return getEntityClass().isAssignableFrom(UserScopedEntity.class);
    }
}
