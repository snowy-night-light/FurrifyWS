package ws.furrify.core.service;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ServiceLogicException;
import ws.furrify.core.model.PluginIntf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PluginRegistryService implements ApplicationContextAware {

    private static ApplicationContext context;

    private final List<PluginIntf> plugins;
    private final Map<String, PluginIntf> pluginMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (PluginIntf plugin : plugins) {
            String simpleName = plugin.getClass().getSimpleName();

            if (pluginMap.containsKey(simpleName)) {
                log.error(Errors.DUPLICATE_STRATEGY_IN_APPLICATION.getErrorMessage(simpleName));

                throw new IllegalStateException(Errors.DUPLICATE_STRATEGY_IN_APPLICATION.getErrorMessage(simpleName));
            }

            pluginMap.put(simpleName, plugin);
        }
    }

    public PluginIntf deserializePlugin(String name) {
        if (!pluginMap.containsKey(name)) {
            throw new ServiceLogicException(Errors.STRATEGY_NOT_FOUND.getErrorMessage(name));
        }

        return pluginMap.get(name);
    }

    public String serializePlugin(PluginIntf strategyIntf) {
        return strategyIntf.getClass().getSimpleName();
    }

    public static PluginRegistryService getInstance() {
        return context.getBean(PluginRegistryService.class);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        context = applicationContext;
    }
}