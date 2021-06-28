package com.kings.framework.leader;

import com.kings.framework.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * <p>
 * 基于Redis的redisson分布式锁实现的简单选举
 * </p>
 *
 * @author lun.wang
 * @date 2021/6/28 11:19 上午
 * @since v1.0
 */
@Slf4j
class RedisLeaderElection implements LeaderElectionRegistry {
    private final LeaderElectionProperties properties;
    private final ScheduledExecutorService leaderElection;
    private final RedisTemplate<String, String> template;
    private static final String LOCAL_HOST = IpUtil.getIp();

    RedisLeaderElection(RedisTemplate<String, String> template, LeaderElectionProperties properties) {
        this.template = template;
        this.properties = properties;
        leaderElection =
                Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, properties.getElectionThreadName()));
    }

    private final List<LeaderElection> elections = new ArrayList<>();

    @Override
    public void register(LeaderElection leaderElection) {
        if (elections.contains(leaderElection)) {
            return;
        }
        elections.add(leaderElection);
    }

    @Override
    public void start() {
        log.debug("RedisLeaderShip startup...");
        //register this server
        template.opsForZSet().add(properties.getGroups(), LOCAL_HOST, LOCAL_HOST.hashCode());
        leaderElection.scheduleWithFixedDelay(new Selector(this.template, this.properties, this.elections), 0,
                properties.getInterval(), properties.getIntervalUnit());
    }

    @Override
    public void destroy() {
        log.debug("RedisLeaderShip shutdown...");
        leaderElection.shutdown();
        //remove this server
        template.opsForZSet().removeRange(properties.getGroups(), 0, -1);
    }

    /**
     * 定时轮询线程 轮询校验本机是否为leader
     * 校验标准参考redis master-election节点值 根据IP hashcode最小值为标准
     */
    static class Selector implements Runnable {
        private final RedisTemplate<String, String> template;
        private final LeaderElectionProperties properties;
        private final List<LeaderElection> elections;

        Selector(RedisTemplate<String, String> template, LeaderElectionProperties properties,
                 List<LeaderElection> elections) {
            this.template = template;
            this.properties = properties;
            this.elections = elections;
        }

        @Override
        public void run() {
            //select at having electors
            if (elections.isEmpty()) {
                return;
            }
            Set<ZSetOperations.TypedTuple<String>> masters =
                    template.opsForZSet().reverseRangeWithScores(properties.getGroups(), 0, 0);
            assert masters != null;
            Optional<ZSetOperations.TypedTuple<String>> optional = masters.stream().findFirst();
            String leader = optional.map(ZSetOperations.TypedTuple::getValue).orElse(null);
            selectCompleted(Objects.equals(leader, LOCAL_HOST));
        }

        private void selectCompleted(final boolean master) {
            if (this.first || this.master != master) {
                this.elections.forEach(elector -> {
                    if (master) {
                        elector.leader();
                    } else {
                        elector.lostLeader();
                    }
                });
                this.master = master;
                this.first = false;
            }
        }

        private boolean master = false;
        private boolean first = true;
    }
}