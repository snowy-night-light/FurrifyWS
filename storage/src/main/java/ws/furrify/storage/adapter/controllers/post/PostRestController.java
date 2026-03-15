package ws.furrify.storage.adapter.controllers.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.post.request.CreatePostRequest;
import ws.furrify.storage.dto.post.request.PatchPostRequest;


@RestController
@RequestMapping("/storage/posts")
class PostRestController extends BaseEntityRestController<Post, PostDTO, CreatePostRequest, PatchPostRequest> {

    @Autowired
    public PostRestController(BaseRequestMapper<Post, PostDTO, CreatePostRequest, PatchPostRequest> requestDtoMapper, BaseEntityCrudService<Post, PostDTO> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
