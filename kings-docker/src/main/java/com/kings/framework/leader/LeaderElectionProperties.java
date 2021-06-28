package com.kings.framework.leader;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author lun.wang
 * @date 2021/6/28 1:47 下午
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "leader.election")
public class LeaderElectionProperties {
    /**
     * 选举实现类型 默认用Redis 目前仅此一种实现
     */
    private Type type = Type.REDIS;
    /**
     * cluster key
     */
    private String electionThreadName = "Leader-Election";
    /**
     * select key
     */
    private String groups = "master-election";
    /**
     * slave轮询选举时间
     */
    private int interval = 5;
    private TimeUnit intervalUnit = TimeUnit.SECONDS;

    enum Type {
        REDIS
    }
}
