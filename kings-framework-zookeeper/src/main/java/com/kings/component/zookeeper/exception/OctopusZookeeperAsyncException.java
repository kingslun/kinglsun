package com.kings.component.zookeeper.exception;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:21 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper异步异常
 * @since v2.7.5
 */
public class OctopusZookeeperAsyncException extends OctopusZookeeperException {
    public OctopusZookeeperAsyncException() {
        super();
    }

    public OctopusZookeeperAsyncException(String message) {
        super(message);
    }

    public OctopusZookeeperAsyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public OctopusZookeeperAsyncException(Throwable cause) {
        super(cause);
    }

    protected OctopusZookeeperAsyncException(String message, Throwable cause,
                                             boolean enableSuppression,
                                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
