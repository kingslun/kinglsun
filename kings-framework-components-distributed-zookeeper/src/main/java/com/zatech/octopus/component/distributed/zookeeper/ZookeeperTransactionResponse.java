package com.zatech.octopus.component.distributed.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>write your description for this class...</p>
 *
 * @author lun.wang
 * @date 2020/4/23 3:37 下午
 * @email lun.wang@zatech.com
 * @from zaintl-common-octopus
 * @title 事物操作结果对象
 * @since v2.7.2
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ZookeeperTransactionResponse implements Serializable {
    private ZookeeperTransactionType operationType;
    private String forPath;
    private String resultPath;
}
