/*
 * furrify-storage-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    protected SourceDTO handlePatch(UUID id, PatchSourceRequest patchDto) {
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

        return super.handlePatch(id, patchDto);
    }

    @Override
    protected SourceDTO handleCreate(SourceDTO dto) {
        if (!dto.getStrategy().validateData(dto.getData())) {
            throw new StrategyDataValidationException(SOURCE_STRATEGY_DATA_VALIDATION_FAILURE.getErrorMessage(null, dto.getStrategy().getClass().getSimpleName(), dto.getData().toString()));
        }

        return super.handleCreate(dto);
    }
}
