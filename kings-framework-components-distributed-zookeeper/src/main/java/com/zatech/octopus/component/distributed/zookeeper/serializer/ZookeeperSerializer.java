package com.zatech.octopus.component.distributed.zookeeper.serializer;

import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperSerializeException;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperSerializerDecorateException;

import java.io.Serializable;

/**
 * <p>
 * 序列化接口 序列化zookeeper读写的value类型 必须包含序列化和反序列化方法
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/6 4:48 下午
 * @email lun.kings@zatech.com
 * @since v2.8.6
 */
public interface ZookeeperSerializer {
    interface ZookeeperSerializerDecorate {
        /**
         * decode the date before serialize
         *
         * @param in data
         * @return out data
         * @throws OctopusZookeeperSerializerDecorateException decode failed
         */
        <I extends Serializable, O> O decode(I in) throws OctopusZookeeperSerializerDecorateException;

        /**
         * encode the bytes to data
         *
         * @param bytes data bytes
         * @param clazz in type
         * @return in data
         * @throws OctopusZookeeperSerializerDecorateException encode failed
         */
        <I extends Serializable> I encode(byte[] bytes, Class<I> clazz)
                throws OctopusZookeeperSerializerDecorateException;
    }

    /**
     * 序列化参数对象
     *
     * @param e 参数对象
     * @return serialized bytes
     * @throws OctopusZookeeperSerializeException 序列化异常
     */
    <E extends Serializable> byte[] serialize(E e) throws OctopusZookeeperSerializeException;

    /**
     * 反序列化
     *
     * @param bytes data bytes
     * @return obj
     * @throws OctopusZookeeperSerializeException 序列化异常
     */
    <E extends Serializable> E deserialize(byte[] bytes) throws OctopusZookeeperSerializeException;
}
