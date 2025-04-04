package me.renzheng.beaker.common.strategy;

import java.util.List;

/**
 * StrategyRegistry
 *
 * @author Renzheng Zhang
 */
public interface StrategyRegistry<T, R> {

    /**
     * 注册策略处理器
     *
     * @param strategyHandler 策略处理器
     */
    void register(StrategyHandler<T, R> strategyHandler);

    /**
     * 注册策略处理器
     *
     * @param strategyHandlers 策略处理器
     */
    void register(List<StrategyHandler<T, R>> strategyHandlers);

    /**
     * 获取策略处理器
     *
     * @param param 参数
     * @return 策略处理器
     */
    StrategyHandler<T, R> get(T param);
}
