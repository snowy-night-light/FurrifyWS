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
package ws.furrify.core.config;

import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import ws.furrify.core.service.ExternalPluginLoaderService;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginConfig {

    private static final String PLUGINS_MOUNT_POINT = "/furrify/plugins";

    @Bean
    public static PluginManager pluginManager(
            ConfigurableApplicationContext applicationContext
    ) {
        Path pluginsRoot = Paths.get(PLUGINS_MOUNT_POINT);

        return new DefaultPluginManager(pluginsRoot) {
            @Override
            protected ExtensionFactory createExtensionFactory() {
                return new ExtensionFactory() {
                    @Override
                    public <T> T create(Class<T> extensionClass) {
                        try {
                            // PF4J creates the plugin using its own classloader.
                            T extension = extensionClass
                                    .getDeclaredConstructor()
                                    .newInstance();

                            // Spring injects @Autowired fields/setters.
                            applicationContext
                                    .getAutowireCapableBeanFactory()
                                    .autowireBean(extension);

                            return extension;

                        } catch (Exception e) {
                            throw new IllegalStateException(
                                    "Failed to instantiate PF4J extension: "
                                            + extensionClass.getName(),
                                    e
                            );
                        }
                    }
                };
            }
        };
    }

    @Bean
    public static ExternalPluginLoaderService externalPluginLoaderService(
            PluginManager pluginManager,
            ConfigurableApplicationContext applicationContext
    ) {
        return new ExternalPluginLoaderService(pluginManager, applicationContext);
    }
}