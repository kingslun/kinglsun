package com.kings.component.zookeeper.exception;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:21 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper事物异常
 * @since v2.7.5
 */
public class OctopusZookeeperTransactionException extends OctopusZookeeperException {
    public OctopusZookeeperTransactionException() {
        super();
    }

    public OctopusZookeeperTransactionException(String message) {
        super(message);
    }

    public OctopusZookeeperTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OctopusZookeeperTransactionException(Throwable cause) {
        super(cause);
    }

    protected OctopusZookeeperTransactionException(String message, Throwable cause,
                                                   boolean enableSuppression,
                                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
