package ws.furrify.attachment.domain.file;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.util.MimeType;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHash;
import ws.furrify.core.entity.UserScopedEntity;

import java.net.URI;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentFile extends UserScopedEntity {
    @Column(nullable = false, length = 255)
    @NotBlank
    private String fileName;

    @Column(nullable = false)
    @NotBlank
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attachment_file_hashes", joinColumns = @JoinColumn(name = "attachment_file_id"))
    private List<AttachmentFileHash> fileHashes;

    @Column(nullable = false, length = 255)
    private MimeType mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileUploadStatus uploadStatus;

    @Min(1)
    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private URI fileUri;

    @Column(nullable = false)
    private URI thumbnailUri;

    @Column(nullable = false)
    private String storageServiceId;
}