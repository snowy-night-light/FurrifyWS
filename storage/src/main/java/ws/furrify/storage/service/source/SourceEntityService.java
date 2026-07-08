package ws.furrify.storage.service.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ReferenceNotFoundException;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.source.request.PatchSourceRequest;
import ws.furrify.storage.shared.exception.StrategyDataValidationException;

import java.util.Optional;
import java.util.UUID;

import static ws.furrify.storage.shared.exception.StorageErrors.SOURCE_STRATEGY_DATA_VALIDATION_FAILURE;

@Service
public class SourceEntityService extends BaseEntityCrudService<Source, SourceDTO, PatchSourceRequest> {

    @Autowired
    public SourceEntityService(BaseEntityRepository<Source> entityRepository, BaseDTOMapper<Source, SourceDTO, PatchSourceRequest> dtoMapper) {
        super(entityRepository, dtoMapper);
    }

    @Override
    @Transactional
    public SourceDTO patchById(UUID id, PatchSourceRequest patchDto) {
        Optional<SourceDTO> sourceDTO = findById(id);
        if (sourceDTO.isEmpty()) {
            throw new ReferenceNotFoundException(Errors.NO_RECORD_FOUND.getErrorMessage(id));
        }

        var newSourceStrategy = patchDto.getSourceStrategy();
        var newData = patchDto.getData();

        if (newSourceStrategy.isPresent() || newData.isPresent()) {
            var strategy = newSourceStrategy.orElseGet(() -> sourceDTO.get().getStrategy());
            var data = newData.orElseGet(() -> sourceDTO.get().getData());

            if (!strategy.validateData(data)) {
                throw new StrategyDataValidationException(SOURCE_STRATEGY_DATA_VALIDATION_FAILURE.getErrorMessage(null, strategy.getClass().getSimpleName(), data.toString()));
            }
        }

        return super.patchById(id, patchDto);
    }

    @Override
    public SourceDTO create(SourceDTO dto) {
        if (!dto.getStrategy().validateData(dto.getData())) {
            throw new StrategyDataValidationException(SOURCE_STRATEGY_DATA_VALIDATION_FAILURE.getErrorMessage(null, dto.getStrategy().getClass().getSimpleName(), dto.getData().toString()));
        }

        return super.create(dto);
    }

    @Override
    protected SourceDTO putById(UUID id, SourceDTO dto) {
        if (!dto.getStrategy().validateData(dto.getData())) {
            throw new StrategyDataValidationException(SOURCE_STRATEGY_DATA_VALIDATION_FAILURE.getErrorMessage(id, dto.getStrategy().getClass().getSimpleName(), dto.getData().toString()));
        }

        return super.putById(id, dto);
    }
}
