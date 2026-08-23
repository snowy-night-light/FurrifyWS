package ws.furrify.storage.domain.media;

import jakarta.persistence.PreRemove;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ws.furrify.openapi.gen.attachment.api.AttachmentFileV1RestControllerApiClient;

@Component
@RequiredArgsConstructor
public class MediaEntityListener {

    private final AttachmentFileV1RestControllerApiClient attachmentFileV1RestControllerApiClient;

    @PreRemove
    public void preRemove(Media media) {
        attachmentFileV1RestControllerApiClient.attachmentFileV1RestControllerDelete(media.getId());
    }
}
