package ws.furrify.storage.domain.post;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.tag.Tag;

import java.util.List;

@Entity
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends UserScopedEntity {
    @Column
    String title;
    @Column
    String description;

    @OneToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    List<Tag> tags;
    @OneToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    List<Artist> artists;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    List<Media> displayMediaList;
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    List<Media> attachments;
}