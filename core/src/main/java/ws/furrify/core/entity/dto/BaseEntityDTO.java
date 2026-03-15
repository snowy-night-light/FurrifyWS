package ws.furrify.core.entity.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class BaseEntityDTO<ENTITY extends BaseEntity> {
    private UUID id;

    private long version;

    private String modifiedBy;
    private ZonedDateTime modifiedAt;

    private String createdBy;
    private ZonedDateTime createdAt;
}
