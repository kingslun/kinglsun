package com.kings.framework.k8s;

import com.kings.framework.K8sPodListener;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Fabric8K8sPodWatcherRegistry implements InitializingBean, DisposableBean {

    @Override
    public void destroy() {
        log.info("K8sNodeListener destroying...");
    }

    @Value("${spring.cloud.kubernetes.client.master-url}")
    private String master;
    @Value("${spring.cloud.kubernetes.client.oauth-token}")
    private String token;

    @Override
    public void afterPropertiesSet() {
        log.info("K8sNodeListener starting...");
        DefaultKubernetesClient client = new DefaultKubernetesClient(
                new ConfigBuilder().withMasterUrl(master).withOauthToken(token)
                        .withTrustCerts(true).build());
        new Fabric8K8sPodWatchRegistry(client).addPodListener("kings", new K8sPodListener() {
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
                log.error("=====>watcher error。。。", e);
            }
        });
        log.info("K8sNodeListener build KubernetesClient success,and now listening");
    }
}
