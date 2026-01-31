package ws.furrify.storage.domain.file;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.util.MimeType;
import ws.furrify.core.entity.BaseEntity;

import java.net.URI;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends BaseEntity {
    @Column(nullable = false, length = 255)
    @NotBlank
    private String fileName;
    @Column(nullable = false, length = 255)
    @NotBlank
    private String fileHash;

    @Column(nullable = false, length = 255)
    private MimeType mimeType;

    @Min(1)
    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private URI fileUri;

    @Column(nullable = false)
    private URI thumbnailUri;
}