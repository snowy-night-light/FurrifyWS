/*
 * furrify-storage-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.storage.controllers.library;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.storage.domain.library.Library;
import ws.furrify.storage.dto.library.LibraryDTO;
import ws.furrify.storage.dto.library.request.CreateLibraryRequest;
import ws.furrify.storage.dto.library.request.PatchLibraryRequest;
import ws.furrify.storage.dto.stats.LibraryStatisticsDto;
import ws.furrify.storage.service.library.LibraryContent;
import ws.furrify.storage.service.library.LibraryContentRemovalService;
import ws.furrify.storage.service.library.LibraryEntityService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@RestController
@RequestMapping("/v1/libraries")
class LibraryV1RestController extends BaseEntityRestController<Library, LibraryDTO, CreateLibraryRequest, PatchLibraryRequest> {

    private final LibraryEntityService libraryService;
    private final LibraryContentRemovalService removalService;

    @Autowired
    public LibraryV1RestController(BaseRequestMapper<Library, LibraryDTO, CreateLibraryRequest> requestDtoMapper,
                                   LibraryEntityService libraryService,
                                   LibraryContentRemovalService removalService) {
        super(requestDtoMapper, libraryService);
        this.libraryService = libraryService;
        this.removalService = removalService;
    }

    @GetMapping(value = "/{id}/statistics", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Counts of what the library holds (books, chapters, posts, collections, tags, tag categories, artists)")
    public LibraryStatisticsDto statistics(@PathVariable UUID id) {
        return libraryService.getStatistics(id);
    }

    /** The library and everything in it (books, posts, collections, tags, artists) go, in the background. */
    @Override
    @DeleteMapping(value = "/{id}", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Delete the library and everything in it, in the background (202; the content disappears gradually)")
    protected void delete(@PathVariable UUID id) {
        libraryService.getById(id); // 404 unless the caller may see it
        removalService.deleteAsync(id);
    }

    @Override
    @DeleteMapping(value = "/bulk", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Delete these libraries and everything in them, in the background")
    protected void deleteBulk(@RequestBody @Valid List<UUID> ids) {
        ids.forEach(libraryService::getById); // all must be the caller's before any goes
        ids.forEach(removalService::deleteAsync);
    }

    /**
     * Remove some of the library's content and keep the library, e.g. {@code ?parts=BOOKS} before a re-import.
     * Media stay (removed later once nothing uses them); 409 while a removal of this library is running.
     */
    @PostMapping(value = "/{id}/clear", produces = {APPLICATION_JSON})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Remove the chosen content of the library (BOOKS, POSTS, COLLECTIONS, TAGS, ARTISTS), in the background")
    public void clear(@PathVariable UUID id, @RequestParam Set<LibraryContent> parts) {
        libraryService.getById(id);
        removalService.clearAsync(id, parts);
    }
}
