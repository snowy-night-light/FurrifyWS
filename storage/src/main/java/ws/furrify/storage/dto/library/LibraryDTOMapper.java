package ws.furrify.storage.dto.library;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.core.model.CycleAvoidingMappingContext;
import ws.furrify.storage.domain.artist.Artist;
import ws.furrify.storage.domain.book.Book;
import ws.furrify.storage.domain.collection.Collection;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.domain.post.Post;
import ws.furrify.storage.domain.tag.Tag;
import ws.furrify.storage.dto.artist.ArtistDTO;
import ws.furrify.storage.dto.artist.ArtistDTOMapper;
import ws.furrify.storage.dto.book.BookDTO;
import ws.furrify.storage.dto.book.BookDTOMapper;
import ws.furrify.storage.dto.collection.CollectionDTO;
import ws.furrify.storage.dto.collection.CollectionDTOMapper;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;
import ws.furrify.storage.dto.post.PostDTO;
import ws.furrify.storage.dto.post.PostDTOMapper;
import ws.furrify.storage.dto.tag.TagDTO;
import ws.furrify.storage.dto.tag.TagDTOMapper;

import java.util.List;

@Mapper(config = BaseDTOMapper.class)
public abstract class LibraryDTOMapper implements BaseDTOMapper<Library, LibraryDTO, PatchLibraryRequest> {

    protected PostDTOMapper postDTOMapper;
    protected TagDTOMapper tagDTOMapper;
    protected ArtistDTOMapper artistDTOMapper;
    protected CollectionDTOMapper collectionDTOMapper;
    protected BookDTOMapper bookDTOMapper;

    @Autowired
    public void setBookDTOMapper(@Lazy BookDTOMapper bookDTOMapper) {
        this.bookDTOMapper = bookDTOMapper;
    }

    @Autowired
    public void setPostDTOMapper(@Lazy PostDTOMapper postDTOMapper) {
        this.postDTOMapper = postDTOMapper;
    }

    @Autowired
    public void setTagDTOMapper(@Lazy TagDTOMapper tagDTOMapper) {
        this.tagDTOMapper = tagDTOMapper;
    }

    @Autowired
    public void setArtistDTOMapper(@Lazy ArtistDTOMapper artistDTOMapper) {
        this.artistDTOMapper = artistDTOMapper;
    }

    @Autowired
    public void setCollectionDTOMapper(@Lazy CollectionDTOMapper collectionDTOMapper) {
        this.collectionDTOMapper = collectionDTOMapper;
    }

    protected List<PostDTO> mapPostList(List<Post> posts, @Context CycleAvoidingMappingContext context) {
        return postDTOMapper.toDtoList(posts, context);
    }

    protected List<Post> mapPostDtoList(List<PostDTO> posts, @Context CycleAvoidingMappingContext context) {
        return postDTOMapper.toEntityList(posts, context);
    }

    protected List<TagDTO> mapTagList(List<Tag> tags, @Context CycleAvoidingMappingContext context) {
        return tagDTOMapper.toDtoList(tags, context);
    }

    protected List<Tag> mapTagDtoList(List<TagDTO> tags, @Context CycleAvoidingMappingContext context) {
        return tagDTOMapper.toEntityList(tags, context);
    }

    protected List<ArtistDTO> mapArtistList(List<Artist> artists, @Context CycleAvoidingMappingContext context) {
        return artistDTOMapper.toDtoList(artists, context);
    }

    protected List<Artist> mapArtistDtoList(List<ArtistDTO> artists, @Context CycleAvoidingMappingContext context) {
        return artistDTOMapper.toEntityList(artists, context);
    }

    protected List<CollectionDTO> mapCollectionList(List<Collection> collections, @Context CycleAvoidingMappingContext context) {
        return collectionDTOMapper.toDtoList(collections, context);
    }

    protected List<Collection> mapCollectionDtoList(List<CollectionDTO> collections, @Context CycleAvoidingMappingContext context) {
        return collectionDTOMapper.toEntityList(collections, context);
    }

    protected List<BookDTO> mapBookList(List<Book> books, @Context CycleAvoidingMappingContext context) {
        return bookDTOMapper.toDtoList(books, context);
    }

    protected List<Book> mapBookDtoList(List<BookDTO> books, @Context CycleAvoidingMappingContext context) {
        return bookDTOMapper.toEntityList(books, context);
    }
}