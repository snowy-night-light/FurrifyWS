/*
 * furrify-worker-service - Furrify Workspace Project
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
package ws.furrify.worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ws.furrify.core.config.CacheConfig;

@Configuration
class CacheConfigImpl extends CacheConfig {

    /**
     * Bounded, expiring cache shared with plugins (they autowire {@link CacheManager}.
     * <p>
     * Tune with {@code FURRIFY_CACHE_CAFFEINE_SPEC}, e.g. {@code maximumSize=500000,expireAfterAccess=12h}.
     */
    @Bean
    @Primary
    CacheManager cacheManager(@Value("${furrify.cache.caffeine-spec:maximumSize=250000,expireAfterAccess=6h,recordStats}") String spec) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheSpecification(spec);
        return manager;
    }
}
