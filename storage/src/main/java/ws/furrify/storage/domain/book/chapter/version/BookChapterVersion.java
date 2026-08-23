package ws.furrify.storage.domain.book.chapter.version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.core.entity.UserScopedEntity;
import ws.furrify.storage.domain.book.chapter.BookChapter;

import jakarta.validation.constraints.Min;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookChapterVersion extends UserScopedEntity {
    @Column(nullable = false)
    @Min(1)
    @NotNull
    Integer chapterVersion;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull
    String content;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull
    String authorNotesEnd;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull
    String authorNotesStart;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "chapter_id")
    BookChapter chapter;
}