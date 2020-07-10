package com.zatech.octopus.component.distributed.zookeeper.exception;

import com.zatech.octopus.module.distributed.exception.OctopusDistributedException;

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
public class OctopusZookeeperSerializeException extends OctopusDistributedException {
    public OctopusZookeeperSerializeException() {
        super();
    }

    public OctopusZookeeperSerializeException(String message) {
        super(message);
    }

    public OctopusZookeeperSerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OctopusZookeeperSerializeException(Throwable cause) {
        super(cause);
    }

    protected OctopusZookeeperSerializeException(String message, Throwable cause, boolean enableSuppression,
                                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
