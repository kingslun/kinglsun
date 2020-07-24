package com.zatech.octopus.component.distributed.zookeeper.config;

import com.zatech.octopus.component.distributed.zookeeper.RetryType;
import com.zatech.octopus.component.distributed.zookeeper.serializer.ZookeeperSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:05 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper自动装配配置文件
 * @since v2.7.5
 */
@ConfigurationProperties(OctopusZookeeperProperties.ZK_PREFIX)
@Getter
@Setter
@ToString
public class OctopusZookeeperProperties {
    public static final String ZK_PREFIX = "octopus.zookeeper";
    public static final String ZK_ELECTION_PREFIX = ZK_PREFIX + ".leaderElection";
    /**
     * 服务器列表，格式host1:port1,host2:port2,…
     */
    private String host;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 重试策略 四种重试策略
     */
    private RetryType retryType = RetryType.EXPONENTIAL_BACKOFF_RETRY;

    /**
     * 会话超时时间，单位毫秒，默认60000ms
     */
    private int sessionTimeoutMs = 60000;

    /**
     * 连接创建超时时间，单位毫秒，默认60000ms
     */
    private int connectionTimeoutMs = 60000;

    /**
     * 只读 如果为true则 create update delete操作为报错
     */
    private boolean readOnly;
    /**
     * 断连重试配置
     */
    @NestedConfigurationProperty
    private Retry retry = new Retry();

    /**
     * 是否监听连接状态
     */
    private boolean listenConnectState;

    @Getter
    @Setter
    @ToString
    public static class Retry {
        /**
         * 重试次数 默认3次
         */
        int retryCount = 3;

        /**
         * 重试间隔数 默认1000毫秒
         */
        int sleepMsBetweenRetries = 1000;
    }

    /**
     * zk client工作线程配置
     */
    @NestedConfigurationProperty
    private Threads threads = new Threads();

    /**
     * zookeeper 线程池配置
     */
    @Getter
    @Setter
    @ToString
    static class Threads {
        /**
         * thread name for zookeeper
         */
        private String name = "OctopusZookeeper";
        /**
         * 核心工作线程数
         */
        private int corePoolSize = Runtime.getRuntime().availableProcessors();
        /**
         * 最大接受工作线程数量
         */
        private int maximumPoolSize = Runtime.getRuntime().availableProcessors();

        /**
         * 当工作线程数大于核心数量 这是多余的空闲线程在终止之前等待新任务的最长时间。默认不等待
         */
        private long keepAliveTime = 0L;

        /**
         * 工作线程可接受的最大数量
         */
        private int workQueueSize = 1024;
    }

    /**
     * 选举配置 默认不使用
     */
    @NestedConfigurationProperty
    private LeaderElection leaderElection = new LeaderElection();

    /*=================================leader election properties=================================*/


    /**
     * zk value序列化类型 默认使用kryo可以选择自己配置 目前支持kryo、jdk两种
     * 也可以自己实现 ZookeeperSerializer 如果自己实现则需要保证此组件在spring容器
     * 如果自己实现则此配置无效
     *
     * @see ZookeeperSerializer
     */
    private Serializer serializer = Serializer.JSON;

    /**
     * zk value序列化类型
     * 目前只提供jdk自带对象序列化 和kryo高性能对象序列化两种方式
     */
    public enum Serializer {
        /**
         * kryo高性能序列化工具
         */
        KRYO,

        /**
         * jdk自带对象序列化
         */
        JDK,

        /**
         * json
         */
        JSON;
    }

    /**
     * leader选举配置
     */
    @Getter
    @Setter
    @ToString
    static class LeaderElection {
        /**
         * leader选举开关 为true则开启选举功能
         */
        private boolean election;

        /**
         * zk选举使用的path
         */
        private String path;
    }
}
