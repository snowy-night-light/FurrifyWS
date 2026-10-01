package ws.furrify.storage.dto.book.chapter.version.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface BookChapterVersionRequestMapper extends BaseRequestMapper<BookChapterVersion, BookChapterVersionDTO, CreateBookChapterVersionRequest> {
    @Override
    BookChapterVersionDTO toDto(CreateBookChapterVersionRequest createBookChapterVersionRequest);
}