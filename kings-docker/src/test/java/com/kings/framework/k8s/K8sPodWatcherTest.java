package com.kings.framework.k8s;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Watch;
import okhttp3.Call;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@SpringBootTest(classes = {KubernetesApiClientConfiguration.class, Fabric8K8sPodWatcherRegistry.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class K8sPodWatcherTest {
    @Autowired
    private ApiClient client;

    @Test
    public void watcher() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        latch.await();
        Assertions.assertThat(latch).isNotNull();
    }

    @Test
    public void watchPod() throws ApiException, IOException {
        CoreV1Api api = new CoreV1Api();
        Call call = api.listNamespacedPodCall("kings", "true", true, null, null,
                null, null, null, null, 36000, true, null);
        try (Watch<V1Pod> watch = Watch.createWatch(client, call, new TypeToken<Watch.Response<V1Pod>>() {
        }.getType())) {
            while (watch.hasNext()) {
                Watch.Response<V1Pod> item = watch.next();
                Assertions.assertThat(item.object.getMetadata()).isNotNull();
                Assertions.assertThat(item.object.getStatus()).isNotNull();
                System.out.printf("name:%s ,type: %s ,status: %s\n", item.object.getMetadata().getName(), item.type,
                        item.object);
            }
        }
    }
}
