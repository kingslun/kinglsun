package com.kings.component.zookeeper;

import com.kings.component.zookeeper.exception.OctopusZookeeperException;
import com.kings.component.zookeeper.exception.OctopusZookeeperSerializeException;
import com.kings.component.zookeeper.serializer.ZookeeperSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

/**
 * <p>最基础的抽象</p>
 *
 * @author lun.wang
 * @date 2020/4/30 6:07 下午
 * @email lun.wang@zatech.com
 * @since v2.7.4
 */
interface Zookeeper extends AutoCloseable, DisposableBean {
    /**
     * 内部线程池获取 可以暴露出去给外部使用
     *
     * @return Executor
     * @throws OctopusZookeeperException failed null pointer
     * @see ExecutorService
     * @see java.util.concurrent.Executor
     */
    ExecutorService threadPool() throws OctopusZookeeperException;

    /**
     * close client or other close work
     *
     * @throws OctopusZookeeperException close failed
     */
    @Override
    void close() throws OctopusZookeeperException;

    /**
     * 销毁函数
     *
     * @throws Exception failure
     */
    @Override
    default void destroy() throws Exception {
        this.close();
    }

    /**
     * init method
     *
     * @throws OctopusZookeeperException open failed
     */
    void open() throws OctopusZookeeperException;

    /**
     * path检测及 自动拼接/
     *
     * @param path source not empty
     * @return src
     */
    default String path0(String path) {
        final String c = "/";
        String inputPath = path.startsWith(c) ? path : c + path;
        return StringUtils.hasText(path) ? inputPath : c;
    }

    /**
     * zk data 序列化
     *
     * @param serializer   序列化器
     * @param data         序列化数据
     * @param <D>          返回数据类型
     * @param <Serializer> 序列器类型
     * @return must extend Serializable
     * @throws OctopusZookeeperSerializeException deserialize failed
     * @author lun.wang
     * @see Serializable
     * @see ZookeeperSerializer
     * @see OctopusZookeeperSerializeException
     * @since v2.8.6
     */
    default <D extends Serializable, Serializer extends ZookeeperSerializer> D deserialize(
            Serializer serializer, byte[] data) throws OctopusZookeeperSerializeException {
        Assert.notNull(serializer, "[ZookeeperSerializer] is null");
        if (data == null || data.length <= 0) {
            return null;
        }
        return serializer.deserialize(data);
    }
}
