package com.zatech.octopus.component.distributed.zookeeper;

import com.kings.framework.common.concurrent.ThreadPool;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateModable;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.Assert;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 7:46 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zk client抽象类 包装公共子类私有函数
 * @since v2.7.2
 */
abstract class AbstractZookeeper<V> implements Zookeeper, ZookeeperWriter<String, V> {
    /**
     * zk client
     */
    protected final CuratorFramework curatorFramework;
    /**
     * 线程池
     */
    protected final ExecutorService executorService;

    protected AbstractZookeeper(CuratorFramework curatorFramework,
                                ExecutorService executorService) {
        Assert.notNull(curatorFramework, "zookeeper client is null");
        Assert.notNull(executorService, "zookeeper client thread pool is null");
        this.curatorFramework = curatorFramework;
        this.executorService = executorService;
    }

    /**
     * zookeeper 与octopus的节点类型转换
     *
     * @param createMode curator mode
     * @param nodeMode   octopus mode
     */
    protected final void withMode(CreateModable<?> createMode, NodeMode nodeMode) {
        switch (nodeMode) {
            case EPHEMERAL:
                createMode.withMode(CreateMode.EPHEMERAL);
                break;
            case PERSISTENT:
                createMode.withMode(CreateMode.PERSISTENT);
                break;
            case EPHEMERAL_SEQUENTIAL:
                createMode.withMode(CreateMode.EPHEMERAL_SEQUENTIAL);
                break;
            case PERSISTENT_SEQUENTIAL:
                createMode.withMode(CreateMode.PERSISTENT_SEQUENTIAL);
                break;
            default:
                break;
        }
    }

    /**
     * 注意：该方法返回一个Stat实例，用于检查ZNode是否存在的操作.
     * 可以调用额外的方法(监控或者后台处理)并在最后调用forPath()指定要操作的ZNode
     *
     * @param s key
     * @return true or false
     * @throws OctopusZookeeperException failed
     */
    @Override
    public boolean nonexistent(String s) throws OctopusZookeeperException {
        try {
            return null == curatorFramework.checkExists().forPath(path0(s));
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 内部线程池获取 可以暴露出去给外部使用
     *
     * @return Executor
     * @see ExecutorService
     * @see Executor
     */
    @Override
    public ExecutorService threadPool() {
        return this.executorService == null ? ThreadPool.availableThreadPool(
                "OctopusZookeeperThread") : this.executorService;
    }

//    /**
//     * close client at destroy
//     *
//     * @throws OctopusZookeeperException close failed
//     */
//    @Override
//    @PreDestroy
//    public void close() throws OctopusZookeeperException {
//        Optional.ofNullable(this.curatorFramework).ifPresent(CloseableUtils::closeQuietly);
//        Optional.ofNullable(this.executorService).ifPresent(ExecutorService::shutdown);
//    }
}
