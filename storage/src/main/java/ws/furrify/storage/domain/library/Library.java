package ws.furrify.storage.domain.library;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.tag.Tag;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Library extends UserScopedEntity {
    @Column(nullable = false, length = 128)
    @Size(max = 128)
    @NotBlank
    String title;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Post> posts;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Tag> tags;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Artist> artists;

    @OneToMany(mappedBy = "library", cascade = CascadeType.REMOVE)
    List<Collection> collections;
}