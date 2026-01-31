package ws.furrify.core.service;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.StrategyRegistryException;
import ws.furrify.core.model.StrategyIntf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyRegistryService implements ApplicationContextAware {

    private static ApplicationContext context;

    private final List<StrategyIntf> strategies;
    private final Map<String, StrategyIntf> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (StrategyIntf strategy : strategies) {
            String simpleName = strategy.getClass().getSimpleName();

            if (strategyMap.containsKey(simpleName)) {
                log.error(Errors.DUPLICATE_STRATEGY_IN_APPLICATION.getErrorMessage(simpleName));

                throw new IllegalStateException(Errors.DUPLICATE_STRATEGY_IN_APPLICATION.getErrorMessage(simpleName));
            }

            strategyMap.put(simpleName, strategy);
        }
    }

    public StrategyIntf deserializeStrategy(String name) {
        if (!strategyMap.containsKey(name)) {
            throw new StrategyRegistryException(Errors.STRATEGY_NOT_FOUND.getErrorMessage(name));
        }

        return strategyMap.get(name);
    }

    public String serializeStrategy(StrategyIntf strategyIntf) {
        return strategyIntf.getClass().getSimpleName();
    }

    public static StrategyRegistryService getInstance() {
        return context.getBean(StrategyRegistryService.class);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        context = applicationContext;
    }
}