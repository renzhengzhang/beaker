package me.renzheng.beaker.common.strategy;

/**
 * DefaultStrategyHandler
 *
 * @author Renzheng Zhang
 */
public class DefaultStrategyHandler<T, R> implements StrategyHandler<T, R> {

    @Override
    public boolean supports(T param) {
        return true;
    }

    @Override
    public R handle(T param) {
        return null;
    }

}
