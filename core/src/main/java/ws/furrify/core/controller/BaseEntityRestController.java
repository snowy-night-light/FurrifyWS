/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.furrify.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.BasePatchEntityRequest;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.core.utils.EntitySpecUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class BaseEntityRestController<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, CREATE_REQ extends BaseCreateEntityRequest<ENTITY, DTO>, PATCH_REQ extends BasePatchEntityRequest<ENTITY, DTO>> {

    private final BaseRequestMapper<ENTITY, DTO, CREATE_REQ> requestDtoMapper;
    private final BaseEntityCrudService<ENTITY, DTO, PATCH_REQ> entityCrudService;

    @GetMapping(value = "/{id}", produces = {APPLICATION_JSON})
    protected ResponseEntity<DTO> getById(@PathVariable UUID id) {
        return entityCrudService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(produces = {APPLICATION_JSON})
    @ResponseStatus(value = HttpStatus.OK)
    protected Page<DTO> getAllPaged(@RequestParam(required = false, name = "spec") String specBase64, Pageable pageable) {
        String decodedSpec = null;
        if (specBase64 != null) {
            decodedSpec = EntitySpecUtils.decodeSpecFromBase64(specBase64);
        }

        return entityCrudService.getAllPaged(decodedSpec, pageable);
    }

    @PostMapping(produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.CREATED)
    protected DTO save(@RequestBody @Valid CREATE_REQ dto) {
        return entityCrudService.create(requestDtoMapper.toDto(dto));
    }

    @PatchMapping(value = "/{id}", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected DTO patch(@PathVariable UUID id, @RequestBody @Valid PATCH_REQ patchRequestDto) {
        return entityCrudService.patchById(id, patchRequestDto);
    }

    @DeleteMapping(value = "/{id}", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected void delete(@PathVariable UUID id) {
        entityCrudService.deleteById(id);
    }

    @PostMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.CREATED)
    protected List<DTO> saveBulk(@RequestBody @Valid List<CREATE_REQ> dtos) {
        return entityCrudService.createBulk(dtos.stream().map(requestDtoMapper::toDto).toList());
    }

    @PatchMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected List<DTO> patchBulk(@RequestBody @Valid Map<UUID, PATCH_REQ> patchRequestDtos) {
        return entityCrudService.patchBulk(patchRequestDtos);
    }

    @DeleteMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    protected void deleteBulk(@RequestBody @Valid List<UUID> ids) {
        entityCrudService.deleteBulk(ids);
    }
}
