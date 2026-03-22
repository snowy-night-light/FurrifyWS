package ws.furrify.storage.service.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.furrify.core.entity.BaseEntityRepository;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.dto.post.PostDTO;

@Service
public class PostEntityCrudService extends BaseEntityCrudService<Post, PostDTO> {

    @Autowired
    public PostEntityCrudService(BaseEntityRepository<Post> entityRepository, BaseDTOMapper<Post, PostDTO> dtoMapper) {
        super(entityRepository, dtoMapper);
    }
}
