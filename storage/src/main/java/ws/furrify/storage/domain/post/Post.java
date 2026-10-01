package ws.furrify.storage.domain.post;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.tag.Tag;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends UserScopedEntity {
    @Column(nullable = false, length = 128)
    @Size(max = 128)
    @NotBlank
    String title;
    @Column(length = 10240)
    @Size(max = 10240)
    String description;

    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @NotEmpty
    List<Tag> tags;
    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    List<Artist> artists;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Media> displayMediaList;
    @ManyToMany(fetch = FetchType.LAZY)
    List<Media> attachments;
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    List<Source> sources;

    @ManyToMany(mappedBy = "posts", fetch = FetchType.LAZY)
    List<Collection> collections;

    @ManyToOne(optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    Library library;
}