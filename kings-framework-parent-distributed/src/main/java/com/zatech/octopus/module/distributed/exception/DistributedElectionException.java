package com.zatech.octopus.module.distributed.exception;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 4:38 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 分布式选举异常
 * @since v2.7.5
 */
public class DistributedElectionException extends OctopusDistributedException {
    public DistributedElectionException() {
        super();
    }

    public DistributedElectionException(String message) {
        super(message);
    }

    public DistributedElectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DistributedElectionException(Throwable cause) {
        super(cause);
    }

    protected DistributedElectionException(String message, Throwable cause,
                                           boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
