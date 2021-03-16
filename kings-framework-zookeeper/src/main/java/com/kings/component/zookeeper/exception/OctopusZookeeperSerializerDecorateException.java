package com.kings.component.zookeeper.exception;

/**
 * <p>序列化错误描述对象</p>
 *
 * @author lun.wang
 * @date 2020/07/06 16:00
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper异常
 * @since v2.8.6
 */
public class OctopusZookeeperSerializerDecorateException extends OctopusZookeeperSerializeException {
    public OctopusZookeeperSerializerDecorateException() {
        super();
    }

    public OctopusZookeeperSerializerDecorateException(String message) {
        super(message);
    }

    public OctopusZookeeperSerializerDecorateException(String message, Throwable cause) {
        super(message, cause);
    }

    public OctopusZookeeperSerializerDecorateException(Throwable cause) {
        super(cause);
    }

    protected OctopusZookeeperSerializerDecorateException(String message, Throwable cause, boolean enableSuppression,
                                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
