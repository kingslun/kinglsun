package com.kings.framework.leader;

/**
 * leader选举
 *
 * @author lun.wang
 * @date 2021/6/28 11:02 上午
 * @since v1.0
 */
public interface LeaderElection {
    /**
     * is leader
     */
    void leader();

    /**
     * lost leader
     */
    void lostLeader();
}
