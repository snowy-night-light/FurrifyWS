package ws.furrify.storage.dto.book;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.dto.book.chapter.BookChapterDTOMapper;
import ws.furrify.storage.dto.book.request.PatchBookRequest;
import ws.furrify.storage.dto.library.LibraryDTOMapper;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {BookChapterDTOMapper.class, LibraryDTOMapper.class}
)
public interface BookDTOMapper extends BaseDTOMapper<Book, BookDTO, PatchBookRequest> {
}