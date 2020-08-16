package com.kings.framework.docker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * <p>
 * docker多实例
 * </p>
 *
 * @author lun.kings
 * @date 2020/8/16 11:32 下午
 * @email lun.wang@zatech.com
 * @since 1.0.0
 */
@SpringBootApplication
@RestController
@Slf4j
public class DockerApplication implements DisposableBean {
    public static void main(String[] args) {
        SpringApplication.run(DockerApplication.class);
    }

    @GetMapping("hello")
    public Mono<String> hello() {
        return Mono.just("hello webflux");
    }

    @Override
    public void destroy() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Application is closing...");
        }
    }
}
