/*
 * furrify-attachment-service - Furrify Workspace Project
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
package ws.furrify.attachment.controller.file;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.attachment.service.file.jxl.JxlFallbackService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Public: an image in its original format for clients without JPEG XL support. Linked from
 * content as {@code <img src>} inside {@code <picture>}, next to the JPEG XL {@code <source>}.
 */
@RestController
@RequestMapping("/v1/files")
class JxlFallbackV1RestController {

    private final JxlFallbackService fallbackService;

    JxlFallbackV1RestController(JxlFallbackService fallbackService) {
        this.fallbackService = fallbackService;
    }

    @Operation(operationId = "JxlFallbackV1RestController_fileFallback")
    @GetMapping("/{id}/fallback")
    public ResponseEntity<Resource> fileFallback(@PathVariable UUID id) throws Exception {
        return serve(fallbackService.fileFallback(id));
    }

    @Operation(operationId = "JxlFallbackV1RestController_thumbnailFallback")
    @GetMapping("/{id}/thumbnail/fallback")
    public ResponseEntity<Resource> thumbnailFallback(@PathVariable UUID id) throws Exception {
        return serve(fallbackService.thumbnailFallback(id));
    }

    private static ResponseEntity<Resource> serve(JxlFallbackService.Fallback fallback) {
        if (fallback == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String type = fallback.mimeType() != null ? fallback.mimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        Resource body = fallback.bytes() != null ? new ByteArrayResource(fallback.bytes()) : new FileSystemResource(fallback.file());
        return ResponseEntity.ok()
                // an attachment's content never changes under its id
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .contentType(MediaType.parseMediaType(type))
                .body(body);
    }
}
