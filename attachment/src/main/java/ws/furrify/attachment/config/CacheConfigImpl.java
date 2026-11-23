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
package ws.furrify.attachment.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ws.furrify.attachment.service.file.jxl.JxlFallbackService;
import ws.furrify.core.config.CacheConfig;

import java.time.Duration;
import java.util.List;

@Configuration
class CacheConfigImpl extends CacheConfig {

    /**
     * {@link JxlFallbackService#FALLBACK_CACHE}: fallbacks made on the fly, kept one hour after
     * they were made. Bounded by their total size (whole images), not by count; tune with
     * {@code FURRIFY_JXL_FALLBACK_CACHE_MAX_BYTES}.
     */
    @Bean
    @Primary
    CacheManager cacheManager(@Value("${furrify.jxl.fallback.cache-max-bytes:536870912}") long maxBytes) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(new CaffeineCache(JxlFallbackService.FALLBACK_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .maximumWeight(maxBytes)
                .<Object, Object>weigher((key, value) -> value instanceof JxlFallbackService.Fallback f ? f.weight() : 1)
                .recordStats()
                .build())));
        return manager;
    }
}
