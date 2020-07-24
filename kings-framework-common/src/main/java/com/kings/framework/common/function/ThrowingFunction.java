package com.kings.framework.common.function;

import java.util.function.Function;

/**
 * @author lun.wang@zatech.com
 * @description 可抛出指定异常的lamda函数表达式
 * @date 2019/8/7 14:00
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    /**
     * 可抛异常的函数
     *
     * @param function function
     * @param t        参数
     * @return R
     * @throws E throwable
     */
    static <T, R, E extends Throwable> R throwing(ThrowingFunction<T, R, E> function, T t) throws E {
        return function.apply(t);
    }

    /**
     * 不可抛异常的函数
     *
     * @param function function
     * @param t        参数
     * @return R
     */
    static <T, R> R unThrowing(Function<T, R> function, T t) {
        return function.apply(t);
    }

    /**
     * 执行入口
     *
     * @param t 参数
     * @return R 返回对象
     * @throws E 异常类型
     */
    R apply(T t) throws E;
}
