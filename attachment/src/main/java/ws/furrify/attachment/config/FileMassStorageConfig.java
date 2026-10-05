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

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.service.file.storage.HostMountpointV1FileMassStorageStrategy;

@Configuration
class FileMassStorageConfig {

    @Bean
    public FileMassStorageStrategy fileMassStorageService() {
        return new HostMountpointV1FileMassStorageStrategy();
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
