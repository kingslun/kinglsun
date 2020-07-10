package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperAsyncException;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperException;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperTransactionException;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperWatcherException;
import com.zatech.octopus.component.distributed.zookeeper.serializer.ZookeeperSerializer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.utils.CloseableUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:39 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper实现 包含分布式一致性和选举能力
 * @since v2.7.5
 */
class ZookeeperProvider extends AbstractZookeeper<Serializable> implements OctopusZookeeper {
    private final ZookeeperTransaction<String, Serializable> transaction;
    private final ZookeeperAsync<String, Serializable> async;
    private final ZookeeperSerializer serializer;

    /**
     * init method
     *
     * @throws OctopusZookeeperException open failed
     */
    @Override
    @PostConstruct
    public void open() throws OctopusZookeeperException {
        this.curatorFramework.start();
    }

    /**
     * 构造实现
     *
     * @param curatorFramework zk client
     * @param executorService  thread pool
     */
    ZookeeperProvider(CuratorFramework curatorFramework, ExecutorService executorService,
                      ZookeeperSerializer serializer) {
        super(curatorFramework, executorService);
        Assert.notNull(serializer, "[ZookeeperSerializer] is null");
        this.serializer = serializer;
        this.transaction = new ZookeeperTransactionProvider(curatorFramework, executorService, serializer);
        this.async = new ZookeeperAsyncProvider(curatorFramework, executorService, serializer);
    }

    /*==========================zookeeper operation==============================*/

    /**
     * 读取一个节点的数据内容
     * 注意，此方法返的返回值是byte[] --> V
     *
     * @param s key
     * @return value
     * @throws OctopusZookeeperException failed
     */
    @Override
    public Serializable get(String s) throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                return null;
            }
            return this.deserialize(this.serializer, curatorFramework.getData().forPath(path0(s)));
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 获取某个节点的所有子节点路径
     * 注意：该方法的返回值为List,获得ZNode的子节点Path列表。 可以调用额外的方法(监控、后台处理或者获取状态watch, background or get stat)
     * 并在最后调用forPath()指定要操作的父ZNode
     *
     * @param s key
     * @return children keys
     * @throws OctopusZookeeperException failed
     */
    @Override
    public String[] children(String s) throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException(
                        String.format("No Path been Created for children by:%s", s));
            }
            final List<String> paths = curatorFramework.getChildren().forPath(path0(s));
            if (CollectionUtils.isEmpty(paths)) {
                return new String[0];
            }
            String[] keys = new String[paths.size()];
            return paths.toArray(keys);
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * CuratorFramework的实例包含inTransaction()接口方法，调用此方法开启一个ZooKeeper事务. 可以复合create, setData,
     * check, and/or delete 等操作然后调用commit()作为一个原子操作提交。
     *
     * @param action 操作
     * @return list of ZookeeperTransactionResponse
     * @throws OctopusZookeeperTransactionException failed
     */
    @Override
    public Collection<ZookeeperTransactionResponse> inTransaction(
            Consumer<ZookeeperTransaction<String, Serializable>> action)
            throws OctopusZookeeperTransactionException {
        //先开启事物
        try (final ZookeeperTransaction<String, Serializable> zookeeperTransaction =
                     this.transaction
                             .startTransaction(this.curatorFramework.inTransaction())) {
            action.accept(zookeeperTransaction);
            //执行完成 提交事物
            return this.transaction.commit();
        } catch (Exception e) {
            throw new OctopusZookeeperTransactionException(e);
        }
    }

    /**
     * 上面提到的创建、删除、更新、读取等方法都是同步的，Curator提供异步接口，引入了BackgroundCallback
     * 接口用于处理异步接口调用之后服务端返回的结果信息。BackgroundCallback接口中一个重要的回调值为CuratorEvent，里面包含事件类型、响应吗和节点的详细信息。
     *
     * @param action 操作
     * @throws OctopusZookeeperAsyncException failed
     */
    @Override
    public OctopusZookeeper inAsync(Consumer<ZookeeperAsync<String, Serializable>> action,
                                    ZookeeperAsyncCallback<String, Serializable> callback,
                                    ZookeeperAsyncErrorListener listener)
            throws OctopusZookeeperAsyncException {
        try (final ZookeeperAsync<String, Serializable> asyncOperator =
                     this.async.openAsync(callback, listener);) {
            action.accept(asyncOperator);
            return this;
        } catch (Exception e) {
            throw new OctopusZookeeperAsyncException(e);
        }
    }

    /*==============================register watcher for path=================================*/

    /**
     * 注册一个指定节点监听器 监听事件包括指定的路径节点的增、删、改的操作 响应的操作会触发监听器的api
     *
     * <p>
     * * 说明：
     * *
     * * 节点路径不存在，set不触发监听
     * * 节点路径不存在，创建事件触发监听（第一次创建时要触发）
     * * 节点路径存在，set触发监听（改操作触发）
     * * 节点路径存在，delete触发监听（删操作触发）
     * *
     * * 节点挂掉，未触发任何监听
     * * 节点重连，未触发任何监听
     * * 节点重连 ，恢复监听
     * *
     * </p>
     *
     * @param path         监听的路径
     * @param pathListener 监听器
     * @throws OctopusZookeeperWatcherException 注册时发生的错误
     * @see ZookeeperPathWatcher {@link ZookeeperPathWatcher#nodeChanged(Object, Object)}
     */
    @Override
    public OctopusZookeeper registerPathWatcher(final String path,
                                                final ZookeeperPathWatcher<String,
                                                        Serializable> pathListener)
            throws OctopusZookeeperWatcherException {
        NodeCache nodeCache = null;
        try {
            if (this.nonexistent(path)) {
                throw new OctopusZookeeperWatcherException(
                        "No Path Been Created For Register Watching");
            }
            nodeCache = new NodeCache(this.curatorFramework, path0(path), false);
            final NodeCache node = nodeCache;
            node.getListenable().addListener(() -> {
                // 节点发生变化，回调方法
                if (node.getCurrentData() != null) {
                    final ChildData currentData = node.getCurrentData();
                    pathListener.nodeChanged(currentData.getPath(),
                            this.deserialize(this.serializer, currentData.getData()));
                } else {
                    //避免不调用和空指针
                    pathListener.nodeChanged("", null);
                }
            });
            // 如果为true则首次不会缓存节点内容到cache中，默认为false,设置为true首次不会触发监听事件
            nodeCache.start();
            return this;
        } catch (Exception e) {
            //close io watcher
            Optional.ofNullable(nodeCache).ifPresent(CloseableUtils::closeQuietly);
            throw new OctopusZookeeperWatcherException("RegisterPathWatcher failed ", e);
        }
    }

    /**
     * 对指定的路径节点的一级子目录进行监听，不对该节点的操作进行监听，对其子目录的节点进行增、删、改的操作监听
     *
     * @param path     监听的节点
     * @param listener 监听器
     * @throws OctopusZookeeperWatcherException 创建监听过程中发生的错误
     */
    @Override
    public OctopusZookeeper registerPathChildrenWatcher(
            final String path, final ZookeeperPathChildrenWatcher<String, Serializable> listener)
            throws OctopusZookeeperWatcherException {
        PathChildrenCache childrenCache = null;
        try {
            Assert.notNull(listener,
                    "listener not be null to register the watcher for path children ");
            if (this.nonexistent(path)) {
                throw new OctopusZookeeperException("No Path Been Created For Register Watching");
            }
            childrenCache = new PathChildrenCache(this.curatorFramework,
                    path0(path), true, false, executorService);
            childrenCache.getListenable().addListener((c, e) -> {
                if (e == null || e.getType() == null || e.getData() == null) {
                    return;
                }
                final PathChildrenCacheEvent.Type type = e.getType();
                final ChildData data = e.getData();
                final String p = data.getPath();
                final Serializable d = this.deserialize(this.serializer, data.getData());
                switch (type) {
                    case CHILD_ADDED:
                        //添加了子节点
                        listener.childAdd(this, p, d);
                        break;
                    case INITIALIZED:
                        //初始化
                        listener.initialized(this, p, d);
                        break;
                    case CHILD_REMOVED:
                        //删除了子节点
                        listener.childRemove(this, p, d);
                        break;
                    case CHILD_UPDATED:
                        //子节点发生变化
                        listener.childUpdate(this, p, d);
                        break;
                    case CONNECTION_LOST:
                        //ZK挂掉一段时间后
                        listener.connectLost(this, p, d);
                        break;
                    case CONNECTION_SUSPENDED:
                        //ZK挂掉
                        listener.connectSuspended(this, p, d);
                        break;
                    case CONNECTION_RECONNECTED:
                        //重新启动ZK curator client is different so will new one
                        listener.connectReconnect(
                                new ZookeeperProvider(c, this.threadPool(), this.serializer), p, d);
                        break;
                    default:
                        break;
                }
            });
            childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            return this;
        } catch (Exception e) {
            Optional.ofNullable(childrenCache).ifPresent(CloseableUtils::closeQuietly);
            throw new OctopusZookeeperWatcherException("RegisterPathChildrenWatcher failed ", e);
        }
    }

    /**
     * 可以将指定的路径节点作为根节点（祖先节点）对其所有的子节点操作进行监听 呈现树形目录的监听
     * 可以设置监听深度 最大监听深度为2147483647（int类型的最大值）
     * * 说明
     * * TreeCache.nodeState == LIVE的时候，才能执行getCurrentChildren非空,默认为PENDING
     * * 初始化完成之后，监听节点操作时 TreeCache.nodeState == LIVE
     * *
     * * maxDepth值设置说明，比如当前监听节点/t1，目录最深为/t1/t2/t3/t4,则maxDepth=3,说明下面3级子目录全
     * * 监听，即监听到t4，如果为2，则监听到t3,对t3的子节点操作不再触发
     * * maxDepth最大值2147483647
     * *
     * * 初次开启监听器会把当前节点及所有子目录节点，触发[type=NODE_ADDED]事件添加所有节点（小等于maxDepth目录）
     * * 默认监听深度至最低层
     * * 初始化以[type=INITIALIZED]结束
     * *
     * *  [type=NODE_UPDATED],set更新节点值操作，范围[当前节点，maxDepth目录节点](闭区间)
     * *
     * *  [type=NODE_ADDED] 增加节点 范围[当前节点，maxDepth目录节点](左闭右闭区间)
     * *
     * *  [type=NODE_REMOVED] 删除节点， 范围[当前节点， maxDepth目录节点](闭区间),删除当前节点无异常
     * *
     * *  事件信息
     * *  TreeCacheEvent{type=NODE_ADDED, data=ChildData{path='/zktest1',
     * stat=4294979373,4294979373,1499850881635,1499850881635,0,0,0,0,2,0,4294979373
     * , data=[116, 49]}}
     *
     * @param path             监听的节点
     * @param listener         监听器
     * @param cacheData        是否缓存节点值
     * @param createParentPath 是否创建父节点
     * @param threadPool       执行工作的线程池
     * @param maxDepthWatcher  最大监听深度 默认为 int最大值
     * @return this
     * @throws OctopusZookeeperWatcherException 创建监听过程中发生的错误
     */
    @Override
    public OctopusZookeeper registerPathAndChildrenWatcher(
            String path, boolean cacheData, boolean createParentPath,
            int maxDepthWatcher, ExecutorService threadPool,
            ZookeeperPathAndChildrenWatcher<String, Serializable> listener)
            throws OctopusZookeeperWatcherException {
        TreeCache treeCache = null;
        try {
            Assert.isTrue(maxDepthWatcher > 0, "maxDepthWatcher mast great by 0 for watcher");
            if (this.nonexistent(path)) {
                throw new OctopusZookeeperWatcherException(
                        "No Path Been Created For Register Watching");
            }
            TreeCache.Builder builder = TreeCache.newBuilder(this.curatorFramework, path0(path));
            builder.setExecutor(threadPool);
            builder.setCacheData(cacheData);
            builder.setCreateParentNodes(createParentPath);
            builder.setMaxDepth(maxDepthWatcher);
            treeCache = builder.build();
            treeCache.start();
            treeCache.getListenable().addListener((client, event) -> {
                if (event == null || event.getType() == null || event.getData() == null) {
                    return;
                }
                final ChildData data = event.getData();
                final String p = data.getPath();
                Serializable d = this.deserialize(this.serializer, data.getData());
                switch (event.getType()) {
                    case NODE_ADDED:
                        listener.pathAdd(this, p, d);
                        break;
                    case NODE_UPDATED:
                        listener.pathUpdate(this, p, d);
                        break;
                    case NODE_REMOVED:
                        listener.pathRemove(this, p, d);
                        break;
                    case CONNECTION_SUSPENDED:
                        listener.connectSuspended(this, p, d);
                        break;
                    case CONNECTION_RECONNECTED:
                        listener.connectReconnect(
                                new ZookeeperProvider(client, this.threadPool(), this.serializer), p, d);
                        break;
                    case CONNECTION_LOST:
                        listener.connectLost(this, p, d);
                        break;
                    case INITIALIZED:
                        listener.initialized(this, p, d);
                        break;
                    default:
                        break;
                }
            });
            return this;
        } catch (Exception e) {
            Optional.ofNullable(treeCache).ifPresent(CloseableUtils::closeQuietly);
            throw new OctopusZookeeperWatcherException("RegisterPathAndChildrenWatcher failed", e);
        }
    }

    /**
     * 创建一个节点 附带初始化内容 并递归操作
     *
     * @param s       key 不可为null
     * @param s2      value 可为null
     * @param mode    节点模式
     * @param recurse 是否递归操作
     * @throws OctopusZookeeperException failed
     * @see NodeMode
     */
    @Override
    public ZookeeperWriter<String, Serializable> create(
            String s, Serializable s2, NodeMode mode, boolean recurse)
            throws OctopusZookeeperException {
        try {
            Assert.notNull(mode, "NodeMode must not be null");
            if (this.nonexistent(path0(s))) {
                CreateBuilder builder = curatorFramework.create();
                //create mode
                super.withMode(builder, mode);
                if (recurse) {
                    builder.creatingParentContainersIfNeeded();
                }
                if (s2 != null) {
                    builder.forPath(path0(s), this.serializer.serialize(s2));
                } else {
                    builder.forPath(path0(s));
                }
                return this;
            } else {
                throw new OctopusZookeeperException(String.format("Node exists for %s", s));
            }
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 删除一个节点，强制指定版本进行删除
     *
     * @param s       key
     * @param version version
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> deleteWithVersion(String s, int version)
            throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException(
                        String.format("No Path been Created for deleteWithVersion by:%s", s));
            }
            curatorFramework.delete().withVersion(version).forPath(path0(s));
            return this;
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 删除一个节点，强制保证删除
     * 接口是一个保障措施，只要客户端会话有效，那么会在后台持续进行删除操作，直到删除节点成功。
     *
     * @param s key
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> deleteForce(String s)
            throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException(
                        String.format("No Path been Created for deleteForce by:%s", s));
            }
            curatorFramework.delete().guaranteed().forPath(path0(s));
            return this;
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 删除一个节点，并且递归删除其所有的子节点
     *
     * @param s       key
     * @param recurse 是否递归
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> delete(String s, boolean recurse)
            throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException(
                        String.format("No Path been Created for delete by:%s", s));
            }
            final DeleteBuilder delete = curatorFramework.delete();
            if (recurse) {
                delete.deletingChildrenIfNeeded();
            }
            delete.forPath(path0(s));
            return this;
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 更新数据节点数据
     * 注意：该接口会返回一个Stat实例
     *
     * @param s  key
     * @param s2 value
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> update(String s, Serializable s2)
            throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException(
                        String.format("No Path been Created for update by:%s", s));
            }
            Assert.notNull(s2, "data must not be empty");
            curatorFramework.setData().forPath(path0(s), this.serializer.serialize(s2));
            return this;
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 更新一个节点的数据内容，强制指定版本进行更新
     *
     * @param s       key
     * @param s2      value
     * @param version version
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> update(String s, Serializable s2, int version)
            throws OctopusZookeeperException {
        try {
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException(
                        String.format("No Path been Created for update by:%s", s));
            }
            Assert.notNull(s2, "data must not be empty");
            curatorFramework.setData().withVersion(version).forPath(path0(s),
                    this.serializer.serialize(s2));
            return this;
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * close client at destroy
     *
     * @throws OctopusZookeeperException close failed
     */
    @Override
    public void close() throws OctopusZookeeperException {
        if (this.async != null) {
            this.async.close();
        }
        if (this.transaction != null) {
            this.transaction.close();
        }
        Optional.of(this.curatorFramework).ifPresent(CloseableUtils::closeQuietly);
        Optional.of(this.executorService).ifPresent(ExecutorService::shutdown);
    }
}
