package ws.furrify.storage.domain.collection;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.post.Post;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Collection extends UserScopedEntity {
    @Column(nullable = true, length = 128)
    @Size(max = 128)
    String title;

    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    List<Post> posts;

    @ManyToOne(optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    Library library;
}