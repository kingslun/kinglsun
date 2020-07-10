package com.zatech.octopus.component.distributed.zookeeper.config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.kings.framework.common.concurrent.KingsThreadFactory;
import com.kings.framework.common.concurrent.ThreadPool;
import com.zatech.octopus.component.distributed.zookeeper.OctopusZookeeper;
import com.zatech.octopus.component.distributed.zookeeper.Zookeeper4DistributedFactory;
import com.zatech.octopus.component.distributed.zookeeper.ZookeeperConnectionStateListener;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperException;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperSerializeException;
import com.zatech.octopus.component.distributed.zookeeper.serializer.ZookeeperSerializer;
import com.zatech.octopus.module.distributed.api.DistributedElection;
import com.zatech.octopus.module.distributed.exception.DistributedElectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static com.zatech.octopus.component.distributed.zookeeper.config.OctopusZookeeperProperties.ZK_ELECTION_PREFIX;

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
public class OctopusZookeeperAutoConfiguration implements InitializingBean,
        ApplicationContextAware, AutoCloseable {

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
        final ThreadFactory kingsThreadFactory =
                KingsThreadFactory.defaultThreadFactory(thread.getName());
        clientBuilder.threadFactory(kingsThreadFactory);
        //thread pool
        final ExecutorService threadPool =
                ThreadPool.threadPool(thread.getName(), thread.getCorePoolSize(),
                        thread.getMaximumPoolSize(), thread.getKeepAliveTime(),
                        thread.getWorkQueueSize());
        //apply this thread pool
        this.threadPool = threadPool;
        //build client
        this.client = clientBuilder.build();
        //connection state listener
        Optional.ofNullable(this.applicationContext).ifPresent(ctx -> {
            final ZookeeperConnectionStateListener connectionStateListener =
                    ctx.getBean(ZookeeperConnectionStateListener.class);
            final ZookeeperSerializer zookeeperSerializer =
                    ctx.getBean(ZookeeperSerializer.class);
            if (connectionStateListener != null && this.properties.isListenConnectState()) {
                //default listener
                this.client.getConnectionStateListenable()
                        .addListener(Zookeeper4DistributedFactory.connectionStateListener(threadPool,
                                connectionStateListener, zookeeperSerializer), threadPool);
            }
        });
    }

    /**
     * 默认zk连接状态监听
     *
     * @return ZookeeperConnectionStateListener
     */
    @Bean
    @ConditionalOnMissingBean(ZookeeperConnectionStateListener.class)
    ZookeeperConnectionStateListener zookeeperConnectionStateListener() {
        return Zookeeper4DistributedFactory.zookeeperConnectionStateListener();
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
        return Zookeeper4DistributedFactory.distributedElection();
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
     * spring ioc context
     */
    private ApplicationContext applicationContext;

    /**
     * spring ioc context
     *
     * @param applicationContext spring auto apply
     * @throws BeansException can throws
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
     * @see ZookeeperValueKryoSerializer
     * @see ZookeeperValueJdkSerializer
     */
    @Bean
    @ConditionalOnMissingBean(ZookeeperSerializer.class)
    ZookeeperSerializer zookeeperSerializer(OctopusZookeeperProperties properties) {
        switch (properties.getSerializer()) {
            case JDK: {
                return new ZookeeperValueJdkSerializer();
            }
            case KRYO: {
                return new ZookeeperValueKryoSerializer();
            }
            default:
                return new ZookeeperValueKryoSerializer();
        }
    }

    /**
     * default zookeeper value serializer by kryo
     * support custom
     * 使用kryo高性能对象序列化工具
     */
    static class ZookeeperValueKryoSerializer implements ZookeeperSerializer {
        /**
         * 默认空对象序列化结果
         */
        private static final byte[] EMPTY_BYTE_ARRAY;
        /**
         * kryo内部使用的thread local
         */
        private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL;

        static {
            EMPTY_BYTE_ARRAY = new byte[0];
            KRYO_THREAD_LOCAL = ThreadLocal.withInitial(Kryo::new);
        }

        /**
         * 序列化对象的class
         */
        private final Class<Object> clazz;

        ZookeeperValueKryoSerializer() {
            clazz = Object.class;
        }

        /**
         * 序列化参数对象
         *
         * @param serializable 参数对象
         * @return serialized bytes
         * @throws OctopusZookeeperSerializeException 序列化异常
         */
        @Override
        public <E extends Serializable> byte[] serialize(E serializable) throws OctopusZookeeperSerializeException {
            if (serializable == null) {
                return EMPTY_BYTE_ARRAY;
            }
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.setReferences(false);
            kryo.register(clazz);
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 Output output = new Output(bos)) {
                kryo.writeClassAndObject(output, serializable);
                output.flush();
                return bos.toByteArray();
            } catch (Exception e) {
                throw new OctopusZookeeperSerializeException("kryo serialize failed " + e.getLocalizedMessage(), e);
            }
        }

        /**
         * 反序列化
         *
         * @param bytes data bytes
         * @return obj
         * @throws OctopusZookeeperSerializeException 序列化异常
         */
        @Override
        public <E extends Serializable> E deserialize(byte[] bytes) throws OctopusZookeeperSerializeException {
            if (bytes == null || bytes.length <= 0) {
                return null;
            }
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.setReferences(false);
            kryo.register(clazz);
            try (Input input = new Input(bytes)) {
                @SuppressWarnings("unchecked")
                E e = (E) kryo.readClassAndObject(input);
                return e;
            } catch (Exception e) {
                throw new OctopusZookeeperSerializeException("kryo deserialize failed " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * zookeeper value serializer by jdk ObjectSerialize
     */
    static class ZookeeperValueJdkSerializer implements ZookeeperSerializer {
        /**
         * 默认空对象序列化结果
         */
        private static final byte[] EMPTY_BYTE_ARRAY;

        static {
            EMPTY_BYTE_ARRAY = new byte[0];
        }

        /**
         * 序列化参数对象
         *
         * @param serializable 参数对象
         * @return serialized bytes
         * @throws OctopusZookeeperSerializeException 序列化异常
         */
        @Override
        public <E extends Serializable> byte[] serialize(E serializable) throws OctopusZookeeperSerializeException {
            if (serializable == null) {
                return EMPTY_BYTE_ARRAY;
            }
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(serializable);
                oos.flush();
                return bos.toByteArray();
            } catch (Exception e) {
                throw new OctopusZookeeperSerializeException("jdk serialize failed " + e.getLocalizedMessage(), e);
            }
        }

        /**
         * 反序列化
         *
         * @param bytes data bytes
         * @return obj
         * @throws OctopusZookeeperSerializeException 序列化异常
         */
        @Override
        public <E extends Serializable> E deserialize(byte[] bytes) throws OctopusZookeeperSerializeException {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                @SuppressWarnings("unchecked")
                E e = (E) ois.readObject();
                return e;
            } catch (Exception e) {
                throw new OctopusZookeeperSerializeException("jdk deserialize failed " + e.getLocalizedMessage(), e);
            }
        }
    }
}
