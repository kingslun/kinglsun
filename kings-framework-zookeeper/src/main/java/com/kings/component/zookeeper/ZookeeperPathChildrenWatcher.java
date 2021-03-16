package com.kings.component.zookeeper;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/22 8:21 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zookeeper指定节点的子节点监听器
 * @since v2.7.2
 */
public interface ZookeeperPathChildrenWatcher<K, V> extends ZookeeperConnectStateWatcher<K, V> {
    /**
     * 添加了子节点
     *
     * @param p        path
     * @param operator zk client
     * @param d        after data
     */
    default void childAdd(OctopusZookeeper operator, K p, V d) {

    }

    /**
     * 删除了子节点
     *
     * @param p        path
     * @param operator zk client
     * @param d        after data
     */
    default void childRemove(OctopusZookeeper operator, K p, V d) {

    }

    /**
     * 子节点发生变化
     *
     * @param p        path
     * @param operator zk client
     * @param d        after data
     */
    default void childUpdate(OctopusZookeeper operator, K p, V d) {

    }
}
