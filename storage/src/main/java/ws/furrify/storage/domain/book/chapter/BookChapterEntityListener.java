package ws.furrify.storage.domain.book.chapter;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class BookChapterEntityListener {

    @PreUpdate
    @Transactional
    public void preUpdate(BookChapter bookChapter) {
        bookChapter.setVersionsCount(bookChapter.getVersions() != null ? bookChapter.getVersions().size() : 0);
    }

    @PrePersist
    @Transactional
    public void prePersist(BookChapter bookChapter) {
        bookChapter.setVersionsCount(bookChapter.getVersions() != null ? bookChapter.getVersions().size() : 0);
    }
}
