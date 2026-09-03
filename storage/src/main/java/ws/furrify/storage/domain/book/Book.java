package ws.furrify.storage.domain.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.media.Media;
import ws.furrify.storage.domain.source.Source;
import ws.furrify.storage.domain.tag.Tag;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Book extends UserScopedEntity {
    @NotBlank
    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false, length = 10240)
    @NotNull
    @Length(max = 10240)
    String descriptionHtml;

    @Column()
    String externalId;

    @Column(columnDefinition = "TEXT", nullable = false, length = 1024)
    @NotNull
    @Length(max = 1024)
    String shortDescriptionHtml;

    @OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    Book sequel;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Book prequel;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Media cover;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Tag> tags;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Artist> artists;

    @Enumerated(EnumType.STRING)
    BookStatus status;

    @Enumerated(EnumType.STRING)
    BookRating rating;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "book")
    List<BookChapter> chapters;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    List<Source> sources;

    @ManyToOne(optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    Library library;

    @Column(nullable = false)
    Long views = 0L;

    @Column(nullable = false)
    Long totalWordCount = 0L;

    @Column(nullable = true)
    Integer likes;

    @Column(nullable = true)
    Integer dislikes;

    @Column(nullable = true)
    ZonedDateTime publishDate;

    @Column(nullable = true)
    ZonedDateTime externalUpdatedAt;
}