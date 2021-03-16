package com.kings.component.zookeeper;

import com.kings.component.zookeeper.exception.OctopusZookeeperException;
import com.kings.component.zookeeper.serializer.ZookeeperSerializer;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/24 5:03 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 查询操作
 * @since v2.7.2
 */
interface ZookeeperReader<K, V> {
    /*=================================query operation===================================*/

    /**
     * 读取一个节点的数据内容
     * 注意，此方法返的返回值是byte[] --> V
     *
     * @param k key
     * @return value if null return empty,may be check is't empty
     * @throws OctopusZookeeperException failed
     */
    default V get(K k) throws OctopusZookeeperException {
        return this.get(k, null);
    }

    /**
     * 读取一个节点的数据内容
     * 注意，此方法返的返回值是byte[] --> V
     * 支持传递序列化器
     *
     * @param k          key
     * @param serializer 序列化参数
     * @return value if null return empty,may be check is't empty
     * @throws OctopusZookeeperException failed
     */
    V get(K k, ZookeeperSerializer serializer) throws OctopusZookeeperException;

    /**
     * 获取某个节点的所有子节点路径
     * 注意：该方法的返回值为List,获得ZNode的子节点Path列表。 可以调用额外的方法(监控、后台处理或者获取状态watch, background or get stat)
     * 并在最后调用forPath()指定要操作的父ZNode
     *
     * @param k key
     * @return children keys
     * @throws OctopusZookeeperException failed
     */
    K[] children(K k) throws OctopusZookeeperException;
}
