package ws.furrify.storage.domain.artist;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;

import java.util.List;

@Entity
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Artist extends UserScopedEntity {
    @ElementCollection
    @CollectionTable(name = "artist_nicknames", joinColumns = @JoinColumn(name = "artist_id"))
    List<ArtistNickname> nicknames;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    List<Source> sources;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    Media avatar;

}