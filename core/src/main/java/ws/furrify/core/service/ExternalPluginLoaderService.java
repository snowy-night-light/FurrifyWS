package ws.furrify.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

@RequiredArgsConstructor
@Slf4j
public class ExternalPluginLoaderService
        implements BeanDefinitionRegistryPostProcessor {

    private final PluginManager pluginManager;
    private final ConfigurableApplicationContext applicationContext;

    private boolean initialized = false;

    /**
     * Called by Spring during the bean-definition phase,
     * before normal singleton beans are instantiated.
     */
    @Override
    public void postProcessBeanDefinitionRegistry(
            BeanDefinitionRegistry registry
    ) {
        if (initialized) {
            return;
        }

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        log.info(
                "Loaded {} PF4J plugins",
                pluginManager.getStartedPlugins().size()
        );

        for (PluginWrapper plugin : pluginManager.getStartedPlugins()) {
            registerPluginComponents(plugin, registry);
        }

        initialized = true;
    }

    private void registerPluginComponents(
            PluginWrapper plugin,
            BeanDefinitionRegistry registry
    ) {
        ClassLoader pluginClassLoader =
                plugin.getPluginClassLoader();

        Set<String> packages = findPluginPackages(plugin);

        if (packages.isEmpty()) {
            log.info(
                    "Plugin {} contains no classes to scan",
                    plugin.getPluginId()
            );
            return;
        }

        ClassPathBeanDefinitionScanner scanner =
                new ClassPathBeanDefinitionScanner(
                        registry,
                        true
                );

        scanner.setResourceLoader(
                new PathMatchingResourcePatternResolver(
                        pluginClassLoader
                )
        );

        /*
         * Pick up:
         *
         * @Component
         * @Service
         * @Repository
         * @Controller
         * etc.
         *
         * Spring stereotype annotations are
         * meta-annotated with @Component.
         */
        scanner.addIncludeFilter(
                new AnnotationTypeFilter(
                        Component.class,
                        true,
                        true
                )
        );

        scanner.setBeanNameGenerator(
                AnnotationBeanNameGenerator.INSTANCE
        );

        for (String packageName : packages) {
            Set<String> before = new HashSet<>(
                    Arrays.asList(registry.getBeanDefinitionNames())
            );

            int registered = scanner.scan(packageName);

            Set<String> added = new HashSet<>(
                    Arrays.asList(registry.getBeanDefinitionNames())
            );

            added.removeAll(before);

            for (String beanName : added) {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);

                if (beanDefinition instanceof AbstractBeanDefinition abd) {
                    String className = beanDefinition.getBeanClassName();

                    if (className == null) {
                        continue;
                    }

                    try {
                        Class<?> beanClass = Class.forName(
                                className,
                                false,
                                pluginClassLoader
                        );

                        abd.setBeanClass(beanClass);

                        log.debug(
                                "Bound plugin bean '{}' to {} using plugin classloader {}",
                                beanName,
                                className,
                                pluginClassLoader
                        );
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(
                                "Cannot load plugin bean class: " + className,
                                e
                        );
                    }
                }
            }

            log.info(
                    "Registered {} Spring beans from plugin {} package {}",
                    registered,
                    plugin.getPluginId(),
                    packageName
            );
        }
    }

    /**
     * Find package roots from the actual plugin artifact.
     *
     * We deliberately do NOT scan the whole classpath.
     */
    private Set<String> findPluginPackages(
            PluginWrapper plugin
    ) {
        Path pluginPath = plugin.getPluginPath();

        if (Files.isDirectory(pluginPath)) {
            return findDirectoryPackages(pluginPath);
        }

        if (!pluginPath.toString().endsWith(".jar")) {
            return Set.of();
        }

        return findJarPackages(pluginPath);
    }

    private Set<String> findJarPackages(Path pluginPath) {
        Set<String> packages = new HashSet<>();

        try (JarFile jarFile = new JarFile(pluginPath.toFile())) {

            jarFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry ->
                            entry.getName().endsWith(".class")
                    )
                    .filter(entry ->
                            !entry.getName().equals("module-info.class")
                    )
                    .filter(entry ->
                            !entry.getName().equals("package-info.class")
                    )
                    .filter(entry ->
                            !entry.getName().contains("$")
                    )
                    .forEach(entry -> {
                        String className =
                                entry.getName()
                                        .replace('/', '.')
                                        .replace(
                                                ".class",
                                                ""
                                        );

                        int lastDot =
                                className.lastIndexOf('.');

                        if (lastDot > 0) {
                            packages.add(
                                    className.substring(0, lastDot)
                            );
                        }
                    });

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to inspect plugin JAR: "
                            + pluginPath,
                    e
            );
        }

        return reduceToTopLevelPackages(packages);
    }

    private Set<String> findDirectoryPackages(Path pluginPath) {
        Set<String> packages = new HashSet<>();

        try {
            Files.walk(pluginPath)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString().endsWith(".class")
                    )
                    .filter(path ->
                            !path.toString().contains("$")
                    )
                    .forEach(path -> {
                        Path relative =
                                pluginPath.relativize(path);

                        String className =
                                relative.toString()
                                        .replace('\\', '.')
                                        .replace('/', '.')
                                        .replace(
                                                ".class",
                                                ""
                                        );

                        int lastDot =
                                className.lastIndexOf('.');

                        if (lastDot > 0) {
                            packages.add(
                                    className.substring(0, lastDot)
                            );
                        }
                    });

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to inspect plugin directory: "
                            + pluginPath,
                    e
            );
        }

        return reduceToTopLevelPackages(packages);
    }

    /**
     * Turn:
     *
     * com.foo
     * com.foo.service
     * com.foo.repository
     * com.foo.strategy
     *
     * into:
     *
     * com.foo
     *
     * This means Spring scans only the plugin's package,
     * rather than the entire application classpath.
     */
    private Set<String> reduceToTopLevelPackages(
            Set<String> packages
    ) {
        Set<String> result = new HashSet<>();

        for (String candidate : packages) {
            boolean childOfExisting = false;

            for (String existing : packages) {
                if (candidate.equals(existing)) {
                    continue;
                }

                if (candidate.startsWith(existing + ".")) {
                    childOfExisting = true;
                    break;
                }
            }

            if (!childOfExisting) {
                result.add(candidate);
            }
        }

        return result;
    }

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory
    ) {
    }

    public <T extends ExtensionPoint> List<T> getPlugins(
            Class<T> extensionType
    ) {
        ensureInitialized();

        return pluginManager.getExtensions(extensionType);
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "Plugin loader has not been initialized yet"
            );
        }
    }
}