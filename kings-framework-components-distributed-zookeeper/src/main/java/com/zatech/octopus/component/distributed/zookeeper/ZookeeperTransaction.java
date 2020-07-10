package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperTransactionException;
import org.apache.curator.framework.api.transaction.CuratorTransaction;

import java.util.Collection;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 3:48 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title zk事物操作
 * @since v2.5.2
 */
public interface ZookeeperTransaction<K, V> extends ZookeeperWriter<K, V>, Zookeeper {

    /**
     * before operate to do
     * must given transaction monitor
     *
     * @param <E> monitor type
     * @param e   monitor
     * @return this
     * @throws OctopusZookeeperTransactionException start failed
     */
    <E extends CuratorTransaction> ZookeeperTransaction<K, V> startTransaction(E e)
            throws OctopusZookeeperTransactionException;

    /**
     * after operate return result collection
     *
     * @return collection
     * @throws OctopusZookeeperTransactionException commit failed
     */
    Collection<ZookeeperTransactionResponse> commit() throws OctopusZookeeperTransactionException;
}
