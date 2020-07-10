package com.zatech.octopus.component.distributed.zookeeper;

/**
 * <p>write your description for this enum...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 3:38 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 事物操作类型
 * @since v2.7.2
 */
enum ZookeeperTransactionType {
    /**
     * 创建
     */
    CREATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 判断
     */
    EXISTS
}
