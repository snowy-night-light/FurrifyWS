package ws.furrify.storage.dto.book.chapter.version;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.storage.domain.book.chapter.version.BookChapterVersion;
import ws.furrify.storage.dto.book.chapter.BookChapterDTOMapper;
import ws.furrify.storage.dto.book.chapter.version.request.PatchBookChapterVersionRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {BookChapterDTOMapper.class}
)
public interface BookChapterVersionDTOMapper extends BaseDTOMapper<BookChapterVersion, BookChapterVersionDTO, PatchBookChapterVersionRequest> {
}