package com.kings.framework;

import com.kings.framework.k8s.DefaultFabric8PodsWatcher;
import com.kings.framework.k8s.Fabric8PodsWatcher;
import com.kings.framework.k8s.K8sPodListener;
import com.kings.framework.leader.LeaderElection;
import com.kings.framework.leader.LeaderElectionRegistry;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 监听Pod事件注册
 * 1.<a href="http://jira.aihuishou.com/browse/ZEUS-45">on production</a>
 * 2.<a href="http://jira.aihuishou.com/browse/ZEUS-44">leader listen</a>
 * </p>
 *
 * @author lun.wang
 * @date 2021/6/23 11:59 上午
 * @since v1.0
 */
@Component
@RestController
@ConditionOnProduction
public class PodEventListener implements ApplicationListener<ApplicationReadyEvent> {
    private static final String[] ENV = {"local"};
    @Value("${spring.cloud.kubernetes.client.master-url}")
    private String master;
    @Value("${spring.cloud.kubernetes.client.oauth-token}")
    private String token;
    @Value("${spring.cloud.kubernetes.client.namespace}")
    private String namespace;
    private final List<Fabric8PodsWatcher> watchers = new ArrayList<>();

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent applicationReadyEvent) {
        //可能对多个环境进行监听
        for (String env : ENV) {
            Config config = new ConfigBuilder().withMasterUrl(master)
                    .withOauthToken(token).withTrustCerts(true).build();
            KubernetesClient client = new DefaultKubernetesClient(config);
            K8sPodListener listener = new PodListener(env);
            Fabric8PodsWatcher watcher = new DefaultFabric8PodsWatcher(client, listener);
            this.watchers.add(watcher);
        }
        //ready master select
        LeaderElectionRegistry registry =
                applicationReadyEvent.getApplicationContext().getBean(LeaderElectionRegistry.class);
        registry.register(new Master(this.watchers, namespace));
    }

    /**
     * 测试关闭监听
     *
     * @return OK
     */
    @GetMapping("/pods/watch/disable")
    public String disable() {
        watchers.forEach(Fabric8PodsWatcher::close);
        return "OK";
    }

    /**
     * 测试重建监听
     *
     * @return OK
     */
    @GetMapping("/pods/watch/enable")
    public String enable() {
        watchers.forEach(watch -> watch.open(namespace));
        return "OK";
    }

    @Slf4j
    private static class PodListener implements K8sPodListener {
        @Override
        public String name() {
            return UUID.randomUUID().toString();
        }

        private final String env;

        private PodListener(String env) {
            Assert.hasText(env, "PodListener must have an environment");
            this.env = env;
        }

        @Override
        public String env() {
            return this.env;
        }

        @Override
        public void onPodShutdown(Pod pod) {
            log.warn("=====>Pod:{} shutdown。。。", pod);
        }

        @Override
        public void onPodCreating(Pod pod) {
            log.info("=====>Pod:{} create。。。", pod);
        }

        @Override
        public void onPodDelete(Pod pod) {
            log.error("=====>Pod:{} delete。。。", pod);
        }

        @Override
        public void onPodUnKnown(Pod pod) {
            log.warn("=====>Pod:{} shutdown。。。", pod);
        }

        @Override
        public void onPodRunning(Pod pod) {
            log.debug("=====>Pod:{} running。。。", pod);
        }

        @Override
        public void onPodPending(Pod pod) {
            log.trace("=====>Pod:{} pending。。。", pod);
        }

        @Override
        public void onException(Exception e) {
            log.error("=====>listen failure。。。");
        }

        @Override
        public void onClose(Exception e) {
            log.error("=====>listener closed。。。");
        }
    }

    @Slf4j
    private static class Master implements LeaderElection {
        private final List<Fabric8PodsWatcher> watchers;
        private final String namespace;

        Master(List<Fabric8PodsWatcher> watchers, String namespace) {
            this.watchers = watchers;
            this.namespace = namespace;
        }

        @Override
        public void leader() {
            log.debug("this cluster become master,now open pod watcher...");
            watchers.forEach(watch -> watch.open(namespace));
        }

        @Override
        public void lostLeader() {
            log.debug("this cluster become slaver,now close pod watcher...");
            watchers.forEach(Fabric8PodsWatcher::close);
        }
    }
}
