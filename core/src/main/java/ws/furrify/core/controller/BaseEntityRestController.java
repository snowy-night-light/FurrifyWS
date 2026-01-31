package ws.furrify.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.entity.BaseRepository;
import ws.furrify.core.entity.dto.BaseEntityDTO;
import ws.furrify.core.entity.dto.BaseEntityDTOMapper;
import ws.furrify.core.entity.dto.BasePatchEntityRequestDTO;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BaseEntityRestController<ENTITY extends BaseEntity, DTO extends BaseEntityDTO<ENTITY>, PATCH_REQ extends BasePatchEntityRequestDTO<ENTITY, DTO>> {

    private final BaseEntityDTOMapper<ENTITY, DTO, PATCH_REQ> mapper;
    private final BaseRepository<ENTITY> repository;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    protected DTO getById(@PathVariable UUID id) {
        return mapper.toDto(repository.getById(id));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    protected Page<DTO> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    protected DTO save(@RequestBody DTO dto) {
        return mapper.toDto(
                this.repository.save(
                        mapper.toEntity(dto)
                )
        );
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    protected Optional<DTO> patch(@PathVariable UUID id, @RequestBody PATCH_REQ patchRequestDto) {
        return repository.findById(id).map(entity -> {
            mapper.patchEntity(patchRequestDto, entity);

            ENTITY savedEntity = repository.save(entity);

            return mapper.toDto(savedEntity);
        });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    protected void delete(@PathVariable UUID id) {
        this.repository.deleteById(id);
    }
}
