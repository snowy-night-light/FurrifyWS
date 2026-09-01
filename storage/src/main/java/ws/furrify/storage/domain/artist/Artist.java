package ws.furrify.storage.domain.artist;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.library.Library;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Artist extends UserScopedEntity {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "artist_nicknames",
            joinColumns = @JoinColumn(name = "artist_id"),
            uniqueConstraints = @UniqueConstraint(name = "UK_artist_nickname", columnNames = {"nickname"})
    )
    @NotEmpty
    List<ArtistNickname> nicknames;

    @Column()
    String externalId;

    @NotNull
    @Column(nullable = false, length = 1024)
    @Length(max = 1024)
    String bioHtml;

    @NotNull
    @Column(nullable = false)
    @PositiveOrZero
    Integer followersCount = 0;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @ToString.Exclude
    List<Source> sources;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    Media avatar;

    @ManyToOne(optional = true)
    @JoinColumn(name = "library_id")
    Library library;
}