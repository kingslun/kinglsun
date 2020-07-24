package com.zatech.octopus.component.distributed.zookeeper;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 10:53 上午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zk连接状态观视者
 * @since v2.5.2
 */
interface ZookeeperConnectStateWatcher<K, V> {
    /**
     * 初始化
     *
     * @param zookeeper zk client
     * @param k         key
     * @param v         after value
     */
    default void initialized(OctopusZookeeper zookeeper, K k, V v) {

    }

    /**
     * ZK挂掉一段时间后
     *
     * @param zookeeper zk client
     * @param k         key
     * @param v         after value
     */
    default void connectLost(OctopusZookeeper zookeeper, K k, V v) {

    }

    /**
     * ZK挂掉
     *
     * @param zookeeper zk client
     * @param k         key
     * @param v         after value
     */
    default void connectSuspended(OctopusZookeeper zookeeper, K k, V v) {

    }

    /**
     * 重新启动ZK
     *
     * @param zookeeper zk client
     * @param k         key
     * @param v         after value
     */
    default void connectReconnect(OctopusZookeeper zookeeper, K k, V v) {

    }
}
