package ws.furrify.storage.dto.book.chapter;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.book.chapter.BookChapter;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.BookDTOMapper;
import ws.furrify.storage.dto.book.chapter.request.PatchBookChapterRequest;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTO;
import ws.furrify.storage.dto.book.chapter.version.BookChapterVersionDTOMapper;
import ws.furrify.storage.dto.source.SourceDTOMapper;

import java.util.List;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {SourceDTOMapper.class}
)
public abstract class BookChapterDTOMapper implements BaseDTOMapper<BookChapter, BookChapterDTO, PatchBookChapterRequest> {

    protected BookChapterVersionDTOMapper bookChapterVersionDTOMapper;
    protected BookDTOMapper bookDTOMapper;
    protected SourceDTOMapper sourceDTOMapper;

    @Autowired
    public void setBookChapterVersionDTOMapper(@Lazy BookChapterVersionDTOMapper bookChapterVersionDTOMapper) {
        this.bookChapterVersionDTOMapper = bookChapterVersionDTOMapper;
    }

    @Autowired
    public void setSourceDTOMapper(SourceDTOMapper sourceDTOMapper) {
        this.sourceDTOMapper = sourceDTOMapper;
    }

    @Autowired
    public void setBookDTOMapper(@Lazy BookDTOMapper bookDTOMapper) {
        this.bookDTOMapper = bookDTOMapper;
    }

    protected BookDTO mapBook(Book book, @Context CycleAvoidingMappingContext context) {
        if (book == null) {
            return null;
        }
        return bookDTOMapper.toDto(book, context);
    }

    protected Book mapBookDto(BookDTO book, @Context CycleAvoidingMappingContext context) {
        if (book == null) {
            return null;
        }
        return bookDTOMapper.toEntity(book, context);
    }

    protected List<BookChapterVersionDTO> mapVersionList(List<BookChapterVersion> versions, @Context CycleAvoidingMappingContext context) {
        if (versions == null) {
            return null;
        }
        return bookChapterVersionDTOMapper.toDtoList(versions, context);
    }

    protected List<BookChapterVersion> mapVersionDtoList(List<BookChapterVersionDTO> versions, @Context CycleAvoidingMappingContext context) {
        if (versions == null) {
            return null;
        }
        return bookChapterVersionDTOMapper.toEntityList(versions, context);
    }

}