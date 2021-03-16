package com.kings.component.zookeeper;

import com.kings.component.zookeeper.api.DistributedElection;
import com.kings.component.zookeeper.exception.DistributedElectionException;
import com.kings.component.zookeeper.exception.OctopusZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/22 11:22 上午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 默认zookeeper-leader选举实现
 * @since v2.7.2
 */
@Slf4j
class Zookeeper4DistributedElection implements InitializingBean, Zookeeper {
    @Override
    public void afterPropertiesSet() throws Exception {
        this.open();
    }

    /**
     * 内部线程池获取 可以暴露出去给外部使用
     *
     * @return Executor
     * @throws OctopusZookeeperException failed null pointer
     * @see ExecutorService
     * @see Executor
     */
    @Override
    public ExecutorService threadPool() throws OctopusZookeeperException {
        throw new OctopusZookeeperException("Not support operation");
    }

    /**
     * init method
     *
     * @throws OctopusZookeeperException open failed
     */
    @Override
    public void open() throws OctopusZookeeperException {
        try {
            leaderLatch.start();
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    private final LeaderLatch leaderLatch;

    Zookeeper4DistributedElection(CuratorFramework client, String path,
                                  DistributedElection distributedElection)
            throws DistributedElectionException {
        try {
            Assert.notNull(client, "zookeeper client is null");
            Assert.notNull(distributedElection, "election monitor is null");
            Assert.hasText(path, "election path for zookeeper client is empty");
            //leader master
            this.leaderLatch = new LeaderLatch(client, path0(path));
            leaderLatch.addListener(new LeaderListener(distributedElection));
        } catch (Exception e) {
            throw new DistributedElectionException(e);
        }
    }

    /**
     * leader listener for zookeeper election
     */
    static class LeaderListener implements LeaderLatchListener {
        @Override
        public void isLeader() {
            if (distributedElection != null) {
                distributedElection.leader();
            }
        }

        @Override
        public void notLeader() {
            if (distributedElection != null) {
                distributedElection.lostLeader();
            }
        }

        private final DistributedElection distributedElection;

        LeaderListener(DistributedElection distributedElection) {
            this.distributedElection = distributedElection;
        }
    }

    /**
     * close method
     *
     * @throws OctopusZookeeperException failed to close
     */
    @Override
    public void close() throws OctopusZookeeperException {
        try {
            leaderLatch.close();
        } catch (IOException e) {
            throw new OctopusZookeeperException(e);
        }
    }
}
