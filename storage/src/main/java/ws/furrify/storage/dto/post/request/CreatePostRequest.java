package ws.furrify.storage.dto.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.core.entity.request.EntityIdRequest;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;

import java.util.List;

@Data
public class CreatePostRequest implements BaseCreateEntityRequest<Post, PostDTO> {

    @NotBlank
    @Length(max = 120)
    private String title;
    @Length(max = 10240)
    private String description;

    @NotEmpty
    private List<@NotNull EntityIdRequest> tags;
    private List<@NotNull EntityIdRequest> artists;

    private List<@NotNull EntityIdRequest> displayMediaList;
    private List<@NotNull EntityIdRequest> attachments;

    private List<@NotNull EntityIdRequest> sources;
}
