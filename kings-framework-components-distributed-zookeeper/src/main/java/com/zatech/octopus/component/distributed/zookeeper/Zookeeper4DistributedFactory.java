package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.component.distributed.zookeeper.serializer.ZookeeperSerializer;
import com.zatech.octopus.module.distributed.api.DistributedElection;
import com.zatech.octopus.module.distributed.exception.DistributedElectionException;
import com.zatech.octopus.module.distributed.exception.OctopusDistributedException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionStateListener;

import java.util.concurrent.ExecutorService;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/22 11:56 上午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title the zookeeper factory
 * @since v2.7.3
 */
public class Zookeeper4DistributedFactory {
    private Zookeeper4DistributedFactory() throws OctopusDistributedException {
        throw new OctopusDistributedException("Not support operation");
    }

    /**
     * 创建zookeeper操作对象
     *
     * @param curatorFramework zk client, not null
     * @param threadPool       thread pool not null
     * @param serializer       value序列化器 @since v2.8.6
     * @return OctopusZookeeper
     * @see ZookeeperProvider
     */
    public static OctopusZookeeper octopusZookeeper(
            CuratorFramework curatorFramework, ExecutorService threadPool,
            ZookeeperSerializer serializer) {
        return new ZookeeperProvider(curatorFramework, threadPool, serializer);
    }

    /**
     * 创建zookeeper分布式选举对象
     *
     * @param client              zk client, not null
     * @param path                zk leader latch path
     * @param distributedElection election applier
     * @return elector
     * @throws DistributedElectionException failed to elect
     */
    public static Zookeeper4DistributedElection createZookeeper4DistributedElector(
            CuratorFramework client, String path,
            DistributedElection distributedElection)
            throws DistributedElectionException {
        return new Zookeeper4DistributedElection(client, path, distributedElection);
    }

    /**
     * connection state monitor
     *
     * @param threadPool              thread pool
     * @param connectionStateListener state listener
     * @param serializer              value序列化器 @since v2.8.6
     * @return ConnectionStateListener
     */
    public static ConnectionStateListener
    connectionStateListener(ExecutorService threadPool,
                            ZookeeperConnectionStateListener connectionStateListener,
                            ZookeeperSerializer serializer) {
        return new ZookeeperConnectionStateMonitor(threadPool, connectionStateListener, serializer);
    }

    /**
     * default connect state listener
     *
     * @return ZookeeperConnectionStateListener
     */
    public static ZookeeperConnectionStateListener zookeeperConnectionStateListener() {
        return new DefaultZookeeperConnectionStateListener();
    }

    /**
     * default elector for distributed service
     *
     * @return DistributedElection
     */
    public static DistributedElection distributedElection() {
        return new DefaultDistributedElection();
    }
}
