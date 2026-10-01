package ws.furrify.storage.dto.book.request;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.BookDTO;

@Mapper(
        config = BaseRequestMapper.class
)
public interface BookRequestMapper extends BaseRequestMapper<Book, BookDTO, CreateBookRequest> {
    @Override
    BookDTO toDto(CreateBookRequest createBookRequest);
}