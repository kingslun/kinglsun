package com.kings.component.zookeeper.exception;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:22 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 分布式一致性异常超类
 * @since v2.7.5
 */
public class OctopusDistributedException extends Exception {
    public OctopusDistributedException() {
        super();
    }

    public OctopusDistributedException(String message) {
        super(message);
    }

    public OctopusDistributedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OctopusDistributedException(Throwable cause) {
        super(cause);
    }

    protected OctopusDistributedException(String message, Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
