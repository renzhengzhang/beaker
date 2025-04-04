package me.renzheng.beaker.common.strategy;

/**
 * 策略处理器
 *
 * @author Renzheng Zhang
 */
public interface StrategyHandler<T, R> {

    /**
     * 是否支持
     *
     * @param param 参数
     * @return 是否支持
     */
    boolean supports(T param);

    /**
     * 执行策略
     *
     * @param param 参数
     * @return 结果
     */
    R handle(T param);

}
