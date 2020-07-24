package com.zatech.octopus.component.distributed.zookeeper;

import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperException;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperTransactionException;
import com.zatech.octopus.component.distributed.zookeeper.serializer.ZookeeperSerializer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.*;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 4:24 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 事物操作实现
 * @since v2.7.2
 */
class ZookeeperTransactionProvider extends AbstractZookeeper<Serializable>
        implements ZookeeperTransaction<String, Serializable> {
    private CuratorTransaction curatorTransaction;
    private CuratorTransactionFinal curatorTransactionFinal;
    /**
     * zk 读写对象序列化工具
     */
    private final ZookeeperSerializer serializer;

    /**
     * init method
     *
     * @throws OctopusZookeeperException open failed
     */
    @Override
    public void open() throws OctopusZookeeperException {

    }

    ZookeeperTransactionProvider(CuratorFramework client, ExecutorService threadPool,
                                 ZookeeperSerializer serializer) {
        super(client, threadPool);
        this.serializer = serializer;
    }

    /**
     * convert operator type
     *
     * @param operationType operate type
     * @return really type
     */
    private ZookeeperTransactionType operationType(OperationType operationType) {
        switch (operationType) {
            case CHECK:
                return ZookeeperTransactionType.EXISTS;
            case CREATE:
                return ZookeeperTransactionType.CREATE;
            case DELETE:
                return ZookeeperTransactionType.DELETE;
            case SET_DATA:
                return ZookeeperTransactionType.UPDATE;
            default:
                return null;
        }
    }

    /**
     * before operate to do
     * must given transaction monitor
     *
     * @param e monitor
     * @throws OctopusZookeeperTransactionException start failed
     */
    @Override
    public <E extends CuratorTransaction> ZookeeperTransaction<String, Serializable>
    startTransaction(E e) throws OctopusZookeeperTransactionException {
        if (e == null) {
            throw new OctopusZookeeperTransactionException(
                    "StartTransaction failed with no monitor");
        }
        if (e instanceof CuratorTransactionFinal) {
            this.curatorTransaction = e;
            this.curatorTransactionFinal = (CuratorTransactionFinal) e;
        } else {
            this.curatorTransaction = e;
            this.curatorTransactionFinal = null;
        }
        return this;
    }

    /**
     * help for GC
     *
     * @throws OctopusZookeeperException close failed
     */
    @Override
    public void close() throws OctopusZookeeperException {
        curatorTransaction = null;
        curatorTransactionFinal = null;
    }

    /**
     * after operate return result collection
     *
     * @return collection
     * @throws OctopusZookeeperTransactionException commit failed
     */
    @Override
    public Collection<ZookeeperTransactionResponse> commit() throws
            OctopusZookeeperTransactionException {
        try {
            if (curatorTransactionFinal != null) {
                final Collection<CuratorTransactionResult> transactionResults =
                        curatorTransactionFinal.commit();
                if (transactionResults.size() < 1) {
                    return Collections.emptyList();
                }
                List<ZookeeperTransactionResponse> ret = new ArrayList<>(transactionResults.size());
                transactionResults.forEach(r ->
                        ret.add(new ZookeeperTransactionResponse(operationType(r.getType()),
                                r.getForPath(), r.getResultPath())));
                return ret;
            } else {
                //return empty or answer not operator
                return Collections.emptyList();
            }
        } catch (Exception e) {
            throw new OctopusZookeeperTransactionException(e);
        } finally {
            //help for GC
            try {
                this.close();
            } catch (OctopusZookeeperException ignore) {
            }
        }
    }

    /**
     * 创建一个节点 附带初始化内容 并递归操作
     *
     * @param s        key 不可为null
     * @param s2       value 可为null
     * @param nodeMode 节点模式
     * @param recurse  是否递归操作
     * @return this
     * @throws OctopusZookeeperException failed
     * @see NodeMode
     */
    @Override
    public ZookeeperWriter<String, Serializable> create(String s, Serializable s2, NodeMode nodeMode,
                                                        boolean recurse)
            throws OctopusZookeeperException {
        try {
            Assert.notNull(this.curatorTransaction, "monitor is null");
            final TransactionCreateBuilder creator = curatorTransaction.create();
            //create mode
            super.withMode(creator, nodeMode);
            final CuratorTransactionBridge transactionBridge;
            if (s2 != null) {
                transactionBridge = creator.forPath(path0(s), this.serializer.serialize(s2));
            } else {
                transactionBridge = creator.forPath(path0(s));
            }
            return this.startTransaction(transactionBridge.and());
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 删除一个节点，强制指定版本进行删除
     *
     * @param s       key
     * @param version version
     * @return this
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> deleteWithVersion(String s, int version)
            throws OctopusZookeeperException {
        try {
            Assert.notNull(this.curatorTransaction, "monitor is null");
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException("No Path been Created for update");
            }
            return this.startTransaction(
                    curatorTransaction.delete().withVersion(version).forPath(path0(s)).and());
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 删除一个节点，强制保证删除
     * 接口是一个保障措施，只要客户端会话有效，那么会在后台持续进行删除操作，直到删除节点成功。
     *
     * @param s key
     * @return this
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> deleteForce(String s)
            throws OctopusZookeeperException {
        try {
            Assert.notNull(this.curatorTransaction, "monitor is null");
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException("No Path been Created for update");
            }
            return this.startTransaction(curatorTransaction.delete().forPath(path0(s)).and());
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 删除一个节点，并且递归删除其所有的子节点
     *
     * @param s       key
     * @param recurse 是否递归
     * @return this
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> delete(String s, boolean recurse)
            throws OctopusZookeeperException {
        return this.deleteForce(s);
    }

    /**
     * 更新数据节点数据
     * 注意：该接口会返回一个Stat实例
     *
     * @param s  key
     * @param s2 value
     * @return this
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> update(String s, Serializable s2)
            throws OctopusZookeeperException {
        try {
            Assert.notNull(s2, "data must not be empty");
            Assert.notNull(this.curatorTransaction, "monitor is null");
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException("No Path been Created for update");
            }
            return this.startTransaction(curatorTransaction.setData()
                    .forPath(path0(s), this.serializer.serialize(s2)).and());
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }

    /**
     * 更新一个节点的数据内容，强制指定版本进行更新
     *
     * @param s       key
     * @param s2      value
     * @param version version
     * @return this
     * @throws OctopusZookeeperException failed
     */
    @Override
    public ZookeeperWriter<String, Serializable> update(String s, Serializable s2, int version)
            throws OctopusZookeeperException {
        try {
            Assert.notNull(s2, "data must not be empty");
            Assert.notNull(this.curatorTransaction, "monitor is null");
            if (this.nonexistent(s)) {
                throw new OctopusZookeeperException("No Path been Created for update");
            }
            return this.startTransaction(curatorTransaction.setData().withVersion(version)
                    .forPath(path0(s), this.serializer.serialize(s2)).and());
        } catch (Exception e) {
            throw new OctopusZookeeperException(e);
        }
    }
}
