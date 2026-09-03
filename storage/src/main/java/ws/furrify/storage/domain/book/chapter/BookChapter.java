package ws.furrify.storage.domain.book.chapter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.domain.source.Source;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookChapter extends UserScopedEntity {
    @NotBlank
    @Column(nullable = false, length = 255)
    String title;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "book_id")
    Book book;

    @Column()
    String externalId;

    @Column(nullable = false)
    Integer chapterNumber;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "chapter")
    List<BookChapterVersion> versions;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    List<Source> sources;

    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    Long views = 0L;

    @Column(nullable = false)
    Long currentNumberOfWords = 0L;

    @Column(nullable = true)
    ZonedDateTime publishDate;
}