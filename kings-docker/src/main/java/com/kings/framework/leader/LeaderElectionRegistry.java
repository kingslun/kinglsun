package com.kings.framework.leader;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public interface LeaderElectionRegistry extends InitializingBean, DisposableBean {
    void register(LeaderElection leaderElection);

    default void afterPropertiesSet() {
        this.start();
    }

    void start();

    void destroy();
}
