package ws.furrify.storage.adapter.controllers.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.post.PostRepository;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.source.SourceRepository;
import ws.furrify.storage.dto.post.PatchPostRequestDTO;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.post.PostDTOMapper;
import ws.furrify.storage.dto.source.PatchSourceRequestDTO;
import ws.furrify.storage.dto.source.SourceDTO;
import ws.furrify.storage.dto.source.SourceDTOMapper;


@RestController
@RequestMapping("/posts")
class PostRestController extends BaseEntityRestController<Post, PostDTO, PatchPostRequestDTO> {

    @Autowired
    public PostRestController(final PostDTOMapper mapper, final PostRepository repository) {
        super(mapper, repository);
    }
}
