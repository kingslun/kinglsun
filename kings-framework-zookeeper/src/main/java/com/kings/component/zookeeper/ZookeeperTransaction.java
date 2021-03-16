package com.kings.component.zookeeper;

import com.kings.component.zookeeper.exception.OctopusZookeeperTransactionException;

import java.util.Collection;

/**
 * <p>zk事物操作</p>
 *
 * @author lun.wang
 * @date 2020/4/23 3:48 下午
 * @email lun.wang@zatech.com
 * @since v2.5.2
 */
public interface ZookeeperTransaction<K, V> extends ZookeeperWriter<K, V>, Zookeeper {

    /**
     * after operate return result collection
     *
     * @return collection
     * @throws OctopusZookeeperTransactionException commit failure
     */
    Collection<ZookeeperTransactionResponse> commit() throws OctopusZookeeperTransactionException;
}
