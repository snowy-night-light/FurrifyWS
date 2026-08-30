package ws.furrify.storage.domain.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.book.chapter.BookChapter;
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
public class Book extends UserScopedEntity {
    @NotBlank
    @Column(nullable = false, length = 128)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false, length = 10240)
    @NotNull
    String descriptionHtml;

    @Column(columnDefinition = "TEXT", nullable = false, length = 1024)
    @NotNull
    String shortDescriptionHtml;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Media cover;

    @OneToMany(fetch = FetchType.EAGER)
    List<Tag> tags;

    @OneToMany(fetch = FetchType.EAGER)
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
}