package com.kings.component.zookeeper;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/24 3:30 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 异常监听 执行报错的时候回调函数
 * @since v2.7.2
 */
@FunctionalInterface
public interface ZookeeperAsyncErrorListener {
    /**
     * failed
     *
     * @param msg       message?
     * @param throwable fail cause
     */
    void onFail(Throwable throwable, String msg);
}
