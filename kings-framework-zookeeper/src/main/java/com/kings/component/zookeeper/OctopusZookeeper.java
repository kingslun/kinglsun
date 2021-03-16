package com.kings.component.zookeeper;

import java.io.Serializable;

/**
 * <p>
 * octopus包装的zookeeper操作接口
 *
 * @author lun.wang
 * @date 2020/07/06 新增value类型支持 支持读写实现了Serializable的对象
 * </p>
 * @date 2020/4/30 5:51 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 对外暴露的zk组件 目前只支持key和value都为string类型的操作
 * 具备连级操作的能力 包含zookeeper基本的读写/事物/异步API
 * @since v2.7.4
 */
public interface OctopusZookeeper
        extends ZookeeperWriter<String, Serializable>, ZookeeperReader<String, Serializable>
        , ZookeeperComplex<String, Serializable> {
    /**
     * 添加连接状态监听器
     *
     * @param listener listener
     * @since v2.8.6
     */
    void connectStateListener(ZookeeperConnectionStateListener listener);
}
