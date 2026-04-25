package ws.furrify.attachment.domain.file;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Column(nullable = true)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attachment_file_hashes", joinColumns = @JoinColumn(name = "attachment_file_id"))
    private List<AttachmentFileHash> fileHashes;

    @Column(nullable = true, length = 255)
    private MimeType mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private FileUploadStatus uploadStatus;

    @Column(nullable = true)
    private Long fileSize;

    @Column(nullable = true)
    private URI fileUri;

    @Column(nullable = true)
    private URI thumbnailUri;

    @Column(nullable = true)
    private String storageServiceId;

}