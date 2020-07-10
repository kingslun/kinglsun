package com.zatech.octopus.component.distributed.zookeeper;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/22 5:00 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 默认的连接状态实现
 * @since v2.7.2
 */
@Slf4j
class DefaultZookeeperConnectionStateListener implements ZookeeperConnectionStateListener {
    /**
     * 连接状态 正常连接成功之后调用一次
     *
     * @param consumer 消费者
     */
    @Override
    public void connected(OctopusZookeeper consumer) {
        if (log.isDebugEnabled()) {
            log.debug("========>>>connected from zookeeper server");
        }
    }

    /**
     * 暂停连接状态 正常暂停连接时调用一次 断开连接也会调用
     *
     * @param consumer 消费者
     */
    @Override
    public void suspended(OctopusZookeeper consumer) {
        if (log.isDebugEnabled()) {
            log.debug("========>>>suspended from zookeeper server");
        }
    }

    /**
     * 重连状态 正常重连成功之后调用一次
     *
     * @param consumer 消费者
     */
    @Override
    public void reconnected(OctopusZookeeper consumer) {
        if (log.isDebugEnabled()) {
            log.debug("========>>>reconnected from zookeeper server");
        }
    }

    /**
     * 连接中断 正常连接被中断之后调用一次 如zk server宕机等
     *
     * @param consumer 消费者
     */
    @Override
    public void lost(OctopusZookeeper consumer) {
        if (log.isDebugEnabled()) {
            log.debug("========>>>lost connected from zookeeper server");
        }
    }

    /**
     * 只读状态 当zk连接被设定为只读时调用一次
     *
     * @param consumer 消费者
     */
    @Override
    public void readOnly(OctopusZookeeper consumer) {
        if (log.isDebugEnabled()) {
            log.debug("========>>>readonly from zookeeper server");
        }
    }
}
