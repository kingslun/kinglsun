package com.kings.component.zookeeper.config;

import com.kings.component.zookeeper.OctopusZookeeper;
import com.kings.component.zookeeper.Zookeeper4DistributedFactory;
import com.kings.component.zookeeper.ZookeeperConnectionStateListener;
import com.kings.component.zookeeper.api.DistributedElection;
import com.kings.component.zookeeper.exception.DistributedElectionException;
import com.kings.component.zookeeper.exception.OctopusZookeeperException;
import com.kings.component.zookeeper.serializer.ZookeeperSerializer;
import com.kings.component.zookeeper.thread.KingsThreadFactory;
import com.kings.component.zookeeper.thread.KingsThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static com.kings.component.zookeeper.config.OctopusZookeeperProperties.ZK_ELECTION_PREFIX;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:01 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper分布式一致性处理方案和选举自动装配对象
 * 包括分布式锁和选举等装配功能
 * @since v2.7.5
 */
@EnableConfigurationProperties(OctopusZookeeperProperties.class)
@Slf4j
public class OctopusZookeeperAutoConfiguration implements InitializingBean, AutoCloseable {

    /**
     * 连接状态监听器管理 处理zk加载完成后自动添加监听器 解耦
     */
    @Configuration
    static class AutoConnectStatusListener implements InitializingBean {
        /**
         * connect state listener
         */
        @Resource
        private ObjectProvider<ZookeeperConnectionStateListener> listener;
        /**
         * zk manager
         */
        @Resource
        private OctopusZookeeper octopusZookeeper;
        /**
         * 配置
         */
        @Resource
        private OctopusZookeeperProperties properties;

        /**
         * 设置连接状态监听器
         *
         * @throws Exception failed
         */
        @Override
        public void afterPropertiesSet() throws Exception {
            if (properties.isListenConnectState()) {
                octopusZookeeper.connectStateListener(listener.getIfAvailable());
            }
        }
    }

    private final OctopusZookeeperProperties properties;
    /**
     * apply by spring post initialize
     */
    private CuratorFramework client;

    /**
     * 线程池
     */
    private ExecutorService threadPool;

    public OctopusZookeeperAutoConfiguration(
            OctopusZookeeperProperties properties) {
        this.properties = properties;
    }

    /**
     * 参数检测
     *
     * @throws Exception fail
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(this.properties.getHost())) {
            throw new OctopusZookeeperException("zookeeper must had a hostname " +
                    "format:[host1:port1,host2:port2,…]");
        }
        if (!StringUtils.hasText(this.properties.getNamespace())) {
            throw new OctopusZookeeperException("zookeeper must had a namespace ");
        }
        if (this.properties.getConnectionTimeoutMs() <= 0) {
            throw new OctopusZookeeperException("connection timeout must great than zero");
        }
        if (properties.getSessionTimeoutMs() <= 0) {
            throw new OctopusZookeeperException("session timeout must great than zero");
        }
        final OctopusZookeeperProperties.LeaderElection leaderElection =
                this.properties.getLeaderElection();
        //leader latch check
        if (leaderElection != null && leaderElection.isElection() &&
                !StringUtils.hasText(leaderElection.getPath())) {
            throw new OctopusZookeeperException("zookeeper open leader election but no path");
        }
        //apply curatorFramework(zk client)
        final CuratorFrameworkFactory.Builder clientBuilder = CuratorFrameworkFactory.builder()
                .connectString(this.properties.getHost())
                .sessionTimeoutMs(this.properties.getSessionTimeoutMs())
                .connectionTimeoutMs(this.properties.getConnectionTimeoutMs())
                .canBeReadOnly(this.properties.isReadOnly())
                .retryPolicy(this.properties.getRetryType().policy(this.properties.getRetry()))
                .namespace(this.properties.getNamespace());
        OctopusZookeeperProperties.Threads thread = this.properties.getThreads();
        //thread factory
        final ThreadFactory threadFactory =
                KingsThreadFactory.defaultThreadFactory(thread.getName());
        clientBuilder.threadFactory(threadFactory);
        //thread pool
        //apply this thread pool
        this.threadPool = KingsThreadPool.threadPool(thread.getName(), thread.getCorePoolSize(),
                thread.getMaximumPoolSize(), thread.getKeepAliveTime(),
                thread.getWorkQueueSize());
        //build client
        this.client = clientBuilder.build();
    }

    /**
     * 默认zk连接状态监听
     *
     * @return ZookeeperConnectionStateListener
     */
    @Bean
    @ConditionalOnMissingBean(ZookeeperConnectionStateListener.class)
    ZookeeperConnectionStateListener zookeeperConnectionStateListener() {
        return new ZookeeperConnectionStateListener() {
            /**
             * 连接状态 正常连接成功之后调用一次
             *
             * @param consumer 消费者
             */
            @Override
            public void connected(OctopusZookeeper consumer) {
                if (log.isDebugEnabled()) {
                    log.debug("=====>>>Zookeeper is connected");
                }
            }

            /**
             * 拒绝连接状态 正常拒绝连接之后调用一次
             *
             * @param consumer 消费者
             */
            @Override
            public void suspended(OctopusZookeeper consumer) {
                if (log.isDebugEnabled()) {
                    log.debug("=====>>>Zookeeper is suspended");
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
                    log.debug("=====>>>Zookeeper is reconnected");
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
                    log.debug("=====>>>Zookeeper is lost connect");
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
                    log.debug("=====>>>Zookeeper is readOnly");
                }
            }
        };
    }

    /**
     * 初始化 操作zookeeper的服务
     *
     * @param zookeeperSerializer zk value serializer
     * @return ZookeeperOperator
     * @see Zookeeper4DistributedFactory#octopusZookeeper
     */
    @Bean
    OctopusZookeeper octopusZookeeper(ZookeeperSerializer zookeeperSerializer) {
        return Zookeeper4DistributedFactory.octopusZookeeper(this.client, this.threadPool, zookeeperSerializer);
    }

    /*=================================leader election autoconfig=================================*/

    /**
     * leader 选举操作默认实现 只有空日志打印
     *
     * @return DistributedElection
     */
    @Bean
    @ConditionalOnMissingBean(DistributedElection.class)
    DistributedElection distributedElection() {
        return new DistributedElection() {
            /**
             * is leader
             * 本机为leader时调用 处理相关逻辑
             */
            @Override
            public void leader() {
                if (log.isDebugEnabled()) {
                    log.debug("=====>>>this machine is leader");
                }
            }

            /**
             * lost leader
             * 本机丢失leader时调用 处理相关逻辑
             */
            @Override
            public void lostLeader() {
                if (log.isDebugEnabled()) {
                    log.debug("=====>>>this machine is no more leader");
                }
            }
        };
    }

    /**
     * 选举处理器
     *
     * @param distributedElection 选举接口
     * @return 选举master leader
     * @throws DistributedElectionException elect failed
     */
    @Bean("Zookeeper4DistributedElector")
    @ConditionalOnProperty(prefix = ZK_ELECTION_PREFIX, name = "election", havingValue = "true")
    Object zookeeper4DistributedElector(DistributedElection distributedElection)
            throws DistributedElectionException {
        return Zookeeper4DistributedFactory.createZookeeper4DistributedElector(this.client,
                this.properties.getLeaderElection().getPath(), distributedElection);
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        Optional.ofNullable(this.threadPool).ifPresent(ExecutorService::shutdownNow);
        Optional.ofNullable(this.client).ifPresent(CuratorFramework::close);
    }

    /**
     * zookeeper value serializer implements
     * default kryo serializer
     *
     * @param properties properties
     * @return ZookeeperValueJdkSerializer, ZookeeperValueKryoSerializer
     * @see com.zatech.octopus.component.distributed.zookeeper.serializer.Serializer
     */
    @Bean
    @ConditionalOnMissingBean(ZookeeperSerializer.class)
    ZookeeperSerializer zookeeperSerializer(OctopusZookeeperProperties properties) {
        return properties.getSerializer().serializer();
    }
}
