package com.zatech.octopus.module.distributed.api;

import com.zatech.octopus.module.distributed.exception.DistributedLockException;

import java.time.Duration;

/**
 * <p>write your description for this interface...
 * 可以由Redis和zookeeper等多种方式实现</p>
 *
 * @param <V> value type
 * @param <K> key type
 * @author lun.wang
 * @date 2020/4/20 3:50 下午
 * @from zaintl-common-octopus
 * @title 分布式一致性解决方案-分布式锁  maybe implement for redis or zookeeper and more
 * @email lun.wang@zatech.com
 * @since v2.7.5
 */
public interface DistributedLock<K, V> {
    /**
     * 获取分布式锁
     * 默认一分钟自动释放
     *
     * @param k key
     * @param v value
     * @return true or false
     */
    default boolean lock(K k, V v) {
        return lock(k, v, Duration.ofMinutes(1));
    }

    /**
     * 获取特定时效分布式锁
     *
     * @param k        key
     * @param v        value
     * @param duration lock release time
     * @return true or false
     */
    boolean lock(K k, V v, Duration duration);

    /**
     * 判断是否已经获取过分布式锁
     *
     * @param k key
     * @param v value
     * @return true or false
     */
    boolean isLock(K k, V v);

    /**
     * 尝试获取分布式锁 默认一分钟自动释放
     *
     * @param k key
     * @param v value
     * @return true or false
     * @throws DistributedLockException lock fail
     */
    default boolean tryLock(K k, V v) throws DistributedLockException {
        return tryLock(k, v, Duration.ofMinutes(1));
    }

    /**
     * 尝试获取特定时效的分布式锁
     *
     * @param k        key
     * @param v        value
     * @param duration time
     * @return true or false
     * @throws DistributedLockException lock fail
     */
    boolean tryLock(K k, V v, Duration duration) throws DistributedLockException;

    /**
     * 释放分布式锁
     *
     * @param k key
     * @param v value
     * @return true or false
     */
    boolean releaseLock(K k, V v);

    /**
     * 释放分布式锁
     *
     * @param k key
     * @param v value
     * @return true or false
     * @throws DistributedLockException
     */
    boolean tryReleaseLock(K k, V v) throws DistributedLockException;
}
