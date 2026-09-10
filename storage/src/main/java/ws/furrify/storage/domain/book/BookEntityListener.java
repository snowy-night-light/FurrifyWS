package ws.furrify.storage.domain.book;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class BookEntityListener {

    @PreUpdate
    public void preUpdate(Book book) {
        book.setChaptersCount(book.getChapters() != null ? book.getChapters().size() : 0);
    }

    @PrePersist
    public void prePersist(Book book) {
        book.setChaptersCount(book.getChapters() != null ? book.getChapters().size() : 0);
    }
}
