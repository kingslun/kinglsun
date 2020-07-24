package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.module.distributed.api.DistributedElection;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/22 1:30 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 默认选举实现
 * @since v2.7.2
 */
@Slf4j
class DefaultDistributedElection implements DistributedElection {
    /**
     * is leader
     */
    @Override
    public void leader() {
        if (log.isDebugEnabled()) {
            log.debug("=====>>> this server is leader");
        }
    }

    /**
     * lost leader
     */
    @Override
    public void lostLeader() {
        if (log.isDebugEnabled()) {
            log.debug("=====>>> this server is leader");
        }
    }
}
