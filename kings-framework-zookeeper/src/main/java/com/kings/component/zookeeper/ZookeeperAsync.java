package com.kings.component.zookeeper;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/24 3:21 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper异步处理标准
 * @since v2.7.2
 */
public interface ZookeeperAsync<K, V> extends ZookeeperWriter<K, V>,
        ZookeeperReader<K, V>, Zookeeper {
    /**
     * open async
     *
     * @param operatorCallback 成功异步回调
     * @param errorListener    失败异步回调
     * @return this
     */
    ZookeeperAsync<K, V> openAsync(ZookeeperAsyncCallback<K, V> operatorCallback,
                                   ZookeeperAsyncErrorListener errorListener);
}
