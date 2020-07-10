package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperException;

/**
 * <p>write your description for this interface...</p>
 *
 * @author lun.wang
 * @date 2020/4/30 5:37 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 可轮询操作的函数 遵循fluent风格的服务
 * <p>因为外界会调用 所以不能为包级私有</p>
 * @since v2.7.4
 */
public interface ZookeeperWriter<K, V> {
    /*======================zookeeper轮询操作==========================*/

    /**
     * 创建一个节点，初始内容为空
     * 如果没有设置节点属性，节点创建模式默认为持久化节点，内容默认为空
     *
     * @param k key
     * @return this
     * @throws OctopusZookeeperException failed
     */
    default ZookeeperWriter<K, V> create(K k) throws OctopusZookeeperException {
        return create(k, null);
    }

    /**
     * 创建一个节点，附带初始化内容
     *
     * @param k key
     * @param v value
     * @return this
     * @throws OctopusZookeeperException failed
     */
    default ZookeeperWriter<K, V> create(K k, V v) throws OctopusZookeeperException {
        return create(k, v, NodeMode.PERSISTENT);
    }

    /**
     * 创建一个节点，附带初始化内容
     *
     * @param k    key 不可为null
     * @param v    value 可为null
     * @param mode 节点模式
     * @return this
     * @throws OctopusZookeeperException failed
     * @see NodeMode
     */
    default ZookeeperWriter<K, V> create(K k, V v, NodeMode mode) throws OctopusZookeeperException {
        return create(k, v, mode, true);
    }

    /**
     * 创建一个节点 附带初始化内容 并递归操作
     *
     * @param k       key 不可为null
     * @param v       value 可为null
     * @param recurse 是否递归操作 无父节点自动创建
     * @param mode    节点模式 临时还是永久及是否带编号 {@link NodeMode}
     * @return this
     * @throws OctopusZookeeperException failed
     * @see NodeMode
     */
    ZookeeperWriter<K, V> create(K k, V v, NodeMode mode, boolean recurse)
            throws OctopusZookeeperException;

    /*=================================delete operation===================================*/

    /**
     * 删除一个节点
     * 注意，此方法只能删除叶子节点，否则会抛出异常。
     *
     * @param k key
     * @return this
     * @throws OctopusZookeeperException failed
     */
    default ZookeeperWriter<K, V> delete(K k) throws OctopusZookeeperException {
        return delete(k, false);
    }

    /**
     * 删除一个节点，强制指定版本进行删除
     *
     * @param k       key
     * @param version version
     * @return this
     * @throws OctopusZookeeperException failed
     */
    ZookeeperWriter<K, V> deleteWithVersion(K k, int version) throws OctopusZookeeperException;

    /**
     * 删除一个节点，强制保证删除
     * 接口是一个保障措施，只要客户端会话有效，那么会在后台持续进行删除操作，直到删除节点成功。
     *
     * @param k key
     * @return this
     * @throws OctopusZookeeperException failed
     */
    ZookeeperWriter<K, V> deleteForce(K k) throws OctopusZookeeperException;

    /**
     * 删除一个节点，并且递归删除其所有的子节点
     *
     * @param k       key
     * @param recurse 是否递归
     * @return this
     * @throws OctopusZookeeperException failed
     */
    ZookeeperWriter<K, V> delete(K k, boolean recurse) throws OctopusZookeeperException;

    /*=================================update operation===================================*/

    /**
     * 更新数据节点数据
     * 注意：该接口会返回一个Stat实例
     *
     * @param k key
     * @param v value
     * @return this
     * @throws OctopusZookeeperException failed
     */
    ZookeeperWriter<K, V> update(K k, V v) throws OctopusZookeeperException;

    /**
     * 更新一个节点的数据内容，强制指定版本进行更新
     *
     * @param k       key
     * @param v       value
     * @param version version
     * @return this
     * @throws OctopusZookeeperException failed
     */
    ZookeeperWriter<K, V> update(K k, V v, int version) throws OctopusZookeeperException;

    /**
     * 注意：该方法返回一个Stat实例，用于检查ZNode是否不存在的操作.
     * 可以调用额外的方法(监控或者后台处理)并在最后调用forPath()指定要操作的ZNode
     *
     * @param k key
     * @return true不存在 or false存在
     * @throws OctopusZookeeperException failed
     */
    boolean nonexistent(K k) throws OctopusZookeeperException;
}
