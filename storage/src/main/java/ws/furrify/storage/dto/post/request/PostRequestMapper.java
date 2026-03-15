package ws.furrify.storage.dto.post.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface PostRequestMapper extends BaseRequestMapper<Post, PostDTO, CreatePostRequest, PatchPostRequest> {
    @Override
    PostDTO toDto(PatchPostRequest patchDto);

    @Override
    PostDTO toDto(CreatePostRequest createPostRequest);
}