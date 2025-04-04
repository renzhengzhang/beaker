package me.renzheng.beaker.common.strategy;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DefaultStrategyRegistry
 *
 * @author Renzheng Zhang
 */
public class DefaultStrategyRegistry<T, R> implements StrategyRegistry<T, R> {

    private final Set<StrategyHandler<T, R>> strategyHandlers;

    public DefaultStrategyRegistry() {
        strategyHandlers = new HashSet<>();
    }

    @Override
    public void register(StrategyHandler<T, R> strategyHandler) {
        strategyHandlers.add(strategyHandler);
    }

    /**
     * 注册策略处理器
     *
     * @param strategyHandlers 策略处理器
     */
    @Override
    public void register(List<StrategyHandler<T, R>> strategyHandlers) {
        if (CollectionUtils.isNotEmpty(strategyHandlers)) {
            this.strategyHandlers.addAll(strategyHandlers);
        }
    }

    /**
     * 获取策略处理器
     *
     * @param param 参数
     * @return 策略处理器
     */
    @Override
    public StrategyHandler<T, R> get(T param) {
        for (StrategyHandler<T, R> strategyHandler : strategyHandlers) {
            if (strategyHandler.supports(param)) {
                return strategyHandler;
            }
        }
        return null;
    }

}
