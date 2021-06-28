package com.kings.framework.leader;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(LeaderElectionProperties.class)
class LeaderAutoConfiguration {

    @Bean
    LeaderElectionRegistry leaderElectionRegistry(RedisTemplate<String, String> redisTemplate,
                                                  LeaderElectionProperties properties) {
        redisTemplate.setDefaultSerializer(new RedisKVSerializer());
        return new RedisLeaderElection(redisTemplate, properties);
    }

    static class RedisKVSerializer implements RedisSerializer<String> {
        @Override
        public byte[] serialize(String s) throws SerializationException {
            return StringUtils.hasText(s) ? s.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }

        @Override
        public String deserialize(byte[] bytes) throws SerializationException {
            return bytes == null || bytes.length < 1 ? null : new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
