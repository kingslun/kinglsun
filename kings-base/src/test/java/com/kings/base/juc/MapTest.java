package com.kings.base.juc;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MapTest {
    @Test
    void currentMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put("key", "value");
        map.forEach(System.out::printf);
        Assertions.assertThat(map.get("key")).isNotEmpty();
    }
}
