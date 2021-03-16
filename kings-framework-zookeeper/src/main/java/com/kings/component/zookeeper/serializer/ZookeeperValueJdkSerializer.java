package com.kings.component.zookeeper.serializer;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/14 6:06 下午
 * @email lun.kings@zatech.com
 * @since v1.0.0
 */

import com.kings.component.zookeeper.exception.OctopusZookeeperSerializeException;

import java.io.*;

/**
 * zookeeper value serializer by jdk ObjectSerialize
 */
class ZookeeperValueJdkSerializer implements ZookeeperSerializer {
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
