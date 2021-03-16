package com.kings.component.zookeeper.serializer;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/14 6:04 下午
 * @email lun.kings@zatech.com
 * @since v1.0.0
 */

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.kings.component.zookeeper.exception.OctopusZookeeperSerializeException;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * default zookeeper value serializer by kryo
 * support custom
 * 使用kryo高性能对象序列化工具
 */
class ZookeeperValueKryoSerializer implements ZookeeperSerializer {
    /**
     * 默认空对象序列化结果
     */
    private static final byte[] EMPTY_BYTE_ARRAY;

    static {
        EMPTY_BYTE_ARRAY = new byte[0];
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
        Kryo kryo = new Kryo();
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
        Kryo kryo = new Kryo();
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
