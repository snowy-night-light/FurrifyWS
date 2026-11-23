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
package ws.furrify.attachment.service.file.jxl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ws.furrify.attachment.domain.file.AttachmentFile;
import ws.furrify.attachment.domain.file.AttachmentFileRepository;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Original-format copies of JPEG XL files for clients without JPEG XL support: the rebuilt
 * original JPEG when the file came from one, otherwise PNG (APNG for animations). Made on the fly
 * and kept in the {@link #FALLBACK_CACHE} cache for one hour (see CacheConfigImpl); nothing is
 * written next to the stored files. Files not stored as JPEG XL are their own fallback (served
 * from disk, not cached).
 */
@Slf4j
@Service
public class JxlFallbackService {

    public static final String FALLBACK_CACHE = "jxlFallbacks";

    /** Either converted bytes or a stored file served as it is. */
    public record Fallback(byte[] bytes, Path file, String mimeType) {
        /** Cache weight in bytes (stored files cost almost nothing to keep). */
        public int weight() {
            return bytes != null ? Math.max(1, bytes.length) : 1;
        }
    }

    private final AttachmentFileRepository repository;
    private final FileMassStorageStrategy storage;
    private final JxlConverter converter;
    private final Semaphore conversions;

    public JxlFallbackService(AttachmentFileRepository repository, FileMassStorageStrategy storage, JxlConverter converter,
                              @Value("${furrify.jxl.fallback.max-concurrent:4}") int maxConcurrent) {
        this.repository = repository;
        this.storage = storage;
        this.converter = converter;
        this.conversions = new Semaphore(Math.max(1, maxConcurrent));
    }

    /** Fallback of the attachment's file; null when there is no such (uploaded) file. */
    @Cacheable(cacheNames = FALLBACK_CACHE, key = "'file:' + #id", unless = "#result == null")
    public Fallback fileFallback(UUID id) throws IOException, InterruptedException {
        AttachmentFile file = repository.findById(id).orElse(null);
        if (file == null || file.getFileUri() == null) return null;
        Path stored = storage.resolve(file.getFileUri());
        if (!stored.toString().endsWith("." + JxlConverter.JXL_EXTENSION)) {
            return Files.exists(stored) ? new Fallback(null, stored, file.getMimeType()) : null;
        }
        boolean jpeg = JxlConverter.isJpeg(file.getOriginalMimeType());
        return new Fallback(decode(stored, jpeg), null, jpeg ? "image/jpeg" : "image/png");
    }

    /** Fallback of the attachment's thumbnail (the thumbnail JPEG); null when it has none. */
    @Cacheable(cacheNames = FALLBACK_CACHE, key = "'thumbnail:' + #id", unless = "#result == null")
    public Fallback thumbnailFallback(UUID id) throws IOException, InterruptedException {
        AttachmentFile file = repository.findById(id).orElse(null);
        URI thumbnail = file == null ? null : file.getThumbnailUri();
        if (thumbnail == null) return null;
        Path stored = storage.resolve(thumbnail);
        if (!stored.toString().endsWith("." + JxlConverter.JXL_EXTENSION)) {
            return Files.exists(stored) ? new Fallback(null, stored, "image/jpeg") : null;
        }
        return new Fallback(decode(stored, true), null, "image/jpeg");
    }

    /** Decode a JPEG XL into its fallback format in a temporary folder and return the bytes. */
    private byte[] decode(Path jxl, boolean jpeg) throws IOException, InterruptedException {
        if (!Files.exists(jxl)) throw new FileNotFoundException(jxl.toString());
        if (!conversions.tryAcquire(60, TimeUnit.SECONDS)) {
            // 503: the caller (e.g. the EPUB generator) tries again later instead of failing for good
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Too many fallback conversions in progress");
        }
        Path dir = Files.createTempDirectory("jxl-fallback-");
        try {
            long start = System.nanoTime();
            Path target = dir.resolve(jpeg ? "fallback.jpg" : "fallback.png");
            converter.decodeFallback(jxl, target, jpeg);
            byte[] bytes = Files.readAllBytes(target);
            log.debug("Fallback of {} made in {}ms ({} bytes)", jxl, (System.nanoTime() - start) / 1_000_000, bytes.length);
            return bytes;
        } finally {
            conversions.release();
            try (var files = Files.walk(dir)) {
                files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }
}
