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
    protected Page<DTO> getAllPaged(Pageable pageable) {
        return entityCrudService.getAllPaged(pageable);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    protected DTO save(@RequestBody @Valid CREATE_REQ dto) {
        return entityCrudService.create(requestDtoMapper.toDto(dto));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    protected DTO patch(@PathVariable UUID id, @RequestBody @Valid PATCH_REQ patchRequestDto) {
        return entityCrudService.patchById(id, patchRequestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    protected void delete(@PathVariable UUID id) {
        entityCrudService.deleteById(id);
    }
}
