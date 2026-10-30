/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
