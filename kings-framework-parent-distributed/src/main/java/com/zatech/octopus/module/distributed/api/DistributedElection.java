package com.zatech.octopus.module.distributed.api;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 4:31 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 分布式选举方案
 * @since v2.7.5
 */
public interface DistributedElection {

    /**
     * is leader
     * 本机为leader时调用 处理相关逻辑
     */
    void leader();

    /**
     * lost leader
     * 本机丢失leader时调用 处理相关逻辑
     */
    void lostLeader();
}
