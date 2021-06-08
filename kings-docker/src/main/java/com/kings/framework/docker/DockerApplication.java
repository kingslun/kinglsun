package com.kings.framework.docker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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

    @GetMapping("index")
    public Mono<Person> index() {
        Person lun = new Person("王伦", 26, "15021261772", "kingslun@163.com", "上海市普陀区曹杨新村", null);
        Person you = new Person("吴优", 3, "15971505417", "wuyou@xinlang.com", "上海市普陀区曹杨二村", lun);
        return Mono.just(you);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    static class Person {
        private String name;
        private int age;
        private String phone;
        private String email;
        private String address;
        private Person husband;
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Application is closing...");
        }
    }

}
