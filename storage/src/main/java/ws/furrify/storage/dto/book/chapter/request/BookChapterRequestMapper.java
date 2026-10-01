package ws.furrify.storage.dto.book.chapter.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.dto.book.chapter.BookChapterDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface BookChapterRequestMapper extends BaseRequestMapper<BookChapter, BookChapterDTO, CreateBookChapterRequest> {
    @Override
    BookChapterDTO toDto(CreateBookChapterRequest createBookChapterRequest);
}