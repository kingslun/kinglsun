package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.module.distributed.api.DistributedLock;
import com.zatech.octopus.module.distributed.exception.DistributedLockException;

import java.time.Duration;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/22 11:20 上午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper分布式锁实现 @TODO
 * @since v2.5.2
 */
class Zookeeper4DistributedLock implements DistributedLock<String, String> {
    /**
     * 获取特定时效分布式锁
     *
     * @param s        key
     * @param s2       value
     * @param duration lock release time
     * @return true or false
     */
    @Override
    public boolean lock(String s, String s2, Duration duration) {
        return false;
    }

    /**
     * 判断是否已经获取过分布式锁
     *
     * @param s  key
     * @param s2 value
     * @return true or false
     */
    @Override
    public boolean isLock(String s, String s2) {
        return false;
    }

    /**
     * 尝试获取特定时效的分布式锁
     *
     * @param s        key
     * @param s2       value
     * @param duration time
     * @return true or false
     * @throws DistributedLockException lock fail
     */
    @Override
    public boolean tryLock(String s, String s2, Duration duration) throws DistributedLockException {
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @param s  key
     * @param s2 value
     * @return true or false
     */
    @Override
    public boolean releaseLock(String s, String s2) {
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @param s  key
     * @param s2 value
     * @return true or false
     * @throws DistributedLockException release failed
     */
    @Override
    public boolean tryReleaseLock(String s, String s2) throws DistributedLockException {
        return false;
    }
}
