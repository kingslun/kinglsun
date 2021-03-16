package com.kings.component.zookeeper.serializer;

/**
 * <p>
 * 序列化器枚举
 * zk value序列化类型
 * * 目前只提供jdk自带对象序列化 和kryo高性能对象序列化两种方式
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/14 6:00 下午
 * @email lun.kings@zatech.com
 * @since v2.8.6
 */
public enum Serializer {
    /**
     * kryo高性能序列化工具
     */
    KRYO {
        @Override
        public ZookeeperSerializer serializer() {
            return KRYO_SERIALIZER;
        }
    },

    /**
     * jdk自带对象序列化
     */
    JDK {
        @Override
        public ZookeeperSerializer serializer() {
            return JDK_SERIALIZER;
        }
    },

    /**
     * custom
     * 自定义实现
     */
    CUSTOM {
        @Override
        public ZookeeperSerializer serializer() {
            throw new UnsupportedOperationException();
        }
    };
    /**
     * kryo serializer
     */
    private final static ZookeeperSerializer KRYO_SERIALIZER = new ZookeeperValueKryoSerializer();
    /**
     * jdk serializer
     */
    private final static ZookeeperSerializer JDK_SERIALIZER = new ZookeeperValueJdkSerializer();

    public abstract ZookeeperSerializer serializer();
}
