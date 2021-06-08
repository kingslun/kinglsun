package com.kings.framework.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Random;

/**
 * <p>
 * 消息生产者
 * </p>
 *
 * @author lun.kings
 * @date 2020/9/11 6:23 下午
 * @email lun.wang@zatech.com
 * @since 1
 */
@SpringBootApplication
@EnableScheduling
public class KafkaApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class);
    }

    @Configuration
    static class MessageProducer {
        @Resource
        private KafkaTemplate<String, String> kafkaTemplate;

        @Scheduled(fixedDelay = 5000L)
        void run() {
            kafkaTemplate.send("kings-topic", new Random().nextLong() + "message");
        }
    }

    @Configuration
    static class MessageConsumer {

        @KafkaListener(topics = "kings-topic", groupId = "kings-group")
        public void onMessage(String message) {
            //insertIntoDb(buffer);//这里为插入数据库代码
            System.out.println(message);
        }
    }
}
