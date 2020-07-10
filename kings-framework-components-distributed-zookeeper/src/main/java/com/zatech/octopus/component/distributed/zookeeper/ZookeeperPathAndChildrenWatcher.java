package com.zatech.octopus.component.distributed.zookeeper;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 10:39 上午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper根节点监听 所有子节点发生变化都会触发事件
 * @since v2.5.2
 */
public interface ZookeeperPathAndChildrenWatcher<K, V> extends ZookeeperConnectStateWatcher<K, V> {
    /**
     * 节点增加
     *
     * @param p        path
     * @param operator zk client
     * @param d        after data
     */
    default void pathAdd(OctopusZookeeper operator, K p, V d) {

    }

    /**
     * 节点删除
     *
     * @param p        path
     * @param operator zk client
     * @param d        after data
     */
    default void pathRemove(OctopusZookeeper operator, K p, V d) {

    }

    /**
     * 节点更新
     *
     * @param p        path
     * @param operator zk client
     * @param d        after data
     */
    default void pathUpdate(OctopusZookeeper operator, K p, V d) {

    }
}
