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
package ws.furrify.worker;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ws.furrify.core.ApplicationCore;
import ws.furrify.core.service.ExternalPluginLoaderService;
import ws.furrify.worker.shared.plugin.WorkerPluginIntf;

import java.util.List;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = {"ws.furrify.worker", "ws.furrify.core"})
@EnableJpaRepositories(basePackages = "ws.furrify.worker.domain")
@RequiredArgsConstructor
public class WorkerApplication extends ApplicationCore implements CommandLineRunner{

    public static final UUID LAUNCH_ID = UUID.randomUUID();

    private final ExternalPluginLoaderService externalPluginLoaderService;

    static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<WorkerPluginIntf> workerPluginIntfList = externalPluginLoaderService.getPlugins(WorkerPluginIntf.class);
        boolean hasDuplicates = workerPluginIntfList.stream()
                .map(WorkerPluginIntf::getProviderName)
                .distinct()
                .count() < workerPluginIntfList.size();

        if (hasDuplicates) {
            throw new IllegalStateException("Duplicate worker plugins found with same provider name! Unable to start.");
        }
    }
}
