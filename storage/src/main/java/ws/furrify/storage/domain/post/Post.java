package ws.furrify.storage.domain.post;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.tag.Tag;

import java.util.Set;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends BaseEntity {
    @Column
    String title;
    @Column
    String description;

    @OneToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    Set<Tag> tags;
    @OneToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    Set<Artist> artists;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Set<Media> displayMediaSet;
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Set<Media> attachments;
}