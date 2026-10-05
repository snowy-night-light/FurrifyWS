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

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.exception.ServiceLogicException;
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
        log.debug("Strategy beans injected: {}", strategies.size());

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
            throw new ServiceLogicException(Errors.STRATEGY_NOT_FOUND.getErrorMessage(name));
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