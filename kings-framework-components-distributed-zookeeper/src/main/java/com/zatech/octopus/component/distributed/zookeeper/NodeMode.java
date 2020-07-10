package com.zatech.octopus.component.distributed.zookeeper;

/**
 * <p>write your description for this enum...</p>
 *
 * @author lun.wang
 * @date 2020/4/20 5:58 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 创建节点的模式
 * @since v2.7.5
 */
public enum NodeMode {
    /**
     * 永久无编号节点
     */
    PERSISTENT,
    /**
     * 永久带编号节点
     */
    PERSISTENT_SEQUENTIAL,
    /**
     * 临时无编号节点
     */
    EPHEMERAL,
    /**
     * 临时带编号节点
     */
    EPHEMERAL_SEQUENTIAL
}
