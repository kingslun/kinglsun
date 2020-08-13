package com.kings.schedule.common.netty.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 4:24 下午
 * @email lun.kings@zatech.com
 * @since
 */

/**
 * default zookeeper value serializer by kryo
 * support custom
 * 使用kryo高性能对象序列化工具
 */
class AbstractKryoSchedulerMessageSerializer<E extends Serializable> implements SchedulerMessageSerializer<E> {
    /**
     * remove thread local
     */
    @PreDestroy
    private void destroy() {
        KRYO_THREAD_LOCAL.remove();
    }

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
    private final Class<E> clazz;

    AbstractKryoSchedulerMessageSerializer(Class<E> clazz) {
        this.clazz = clazz;
    }

    /**
     * 序列化参数对象
     *
     * @param serial 参数对象
     * @return serialized bytes
     * @throws SchedulerMessageSerializeException 序列化异常
     */
    @Override
    public byte[] serialize(E serial) throws SchedulerMessageSerializeException {
        if (serial == null) {
            return EMPTY_BYTE_ARRAY;
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        kryo.setReferences(false);
        kryo.register(clazz);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            kryo.writeClassAndObject(output, serial);
            output.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new SchedulerMessageSerializeException("kryo serialize failed " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * 反序列化
     *
     * @param bytes data bytes
     * @return obj
     * @throws SchedulerMessageSerializeException 序列化异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public E deserialize(byte[] bytes) throws SchedulerMessageSerializeException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        kryo.setReferences(false);
        kryo.register(clazz);
        try (Input input = new Input(bytes)) {
            return (E) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new SchedulerMessageSerializeException("kryo deserialize failed " + e.getLocalizedMessage(), e);
        }
    }
}
