/*
 * furrify-core - Furrify Workspace Project
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
package ws.furrify.core.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EurekaDiscoveryService {
    private final EurekaClient eurekaClient;

    public boolean isServiceOnline(String serviceName) {
        Application application = eurekaClient.getApplication(serviceName.toUpperCase());
        if (application == null || application.getInstances().isEmpty()) {
            return false;
        }

        return application.getInstances().stream()
                .anyMatch(instance -> instance.getStatus() == InstanceInfo.InstanceStatus.UP);
    }

    public List<InstanceInfo> getServiceInstances(String serviceName) {
        Application application = eurekaClient.getApplication(serviceName.toUpperCase());
        if (application == null || application.getInstances().isEmpty()) {
            return Collections.emptyList();
        }

        return application.getInstances();
    }
}
