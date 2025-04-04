package me.renzheng.beaker.common.strategy;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * AbstractStrategyRouter
 *
 * @author Renzheng Zhang
 */
public abstract class AbstractStrategyRouter<T, R> implements InitializingBean {

    private StrategyRegistry<T, R> strategyRegistry;

    @Resource
    private List<StrategyHandler<T, R>> strategyHandlers;

    public StrategyHandler<T, R> route(T t) {
        return strategyRegistry.get(t);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        strategyRegistry = new DefaultStrategyRegistry<>();
        strategyRegistry.register(strategyHandlers);
    }

}