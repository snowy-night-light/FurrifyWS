package ws.furrify.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<DTO> extends JpaRepository<DTO, UUID> {
    DTO getById(UUID id);
}
