package com.kings.schedule.common.netty.serialize;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 4:25 下午
 * @email lun.kings@zatech.com
 * @since v1.0.0
 */
public interface SchedulerMessageSerializer<T extends Serializable> {
    /**
     * 序列化参数对象
     *
     * @param serializable 参数对象
     * @return serialized bytes
     * @throws SchedulerMessageSerializeException 序列化异常
     */
    byte[] serialize(T serializable) throws SchedulerMessageSerializeException;

    /**
     * 反序列化
     *
     * @param bytes data bytes
     * @return obj
     * @throws SchedulerMessageSerializeException 序列化异常
     */
    T deserialize(byte[] bytes) throws SchedulerMessageSerializeException;
}
