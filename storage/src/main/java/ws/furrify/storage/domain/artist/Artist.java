package ws.furrify.storage.domain.artist;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.storage.domain.artist.vo.ArtistNickname;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;

import java.util.Set;

@Entity
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Artist extends BaseEntity {
    @ElementCollection
    @CollectionTable(name = "artist_nicknames", joinColumns = @JoinColumn(name = "artist_id"))
    Set<ArtistNickname> nicknames;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    Set<Source> sources;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    Media avatar;

}