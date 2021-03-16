package com.kings.component.zookeeper;

import com.kings.component.zookeeper.exception.OctopusZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(classes = OctopusZookeeperTest.class)
@RunWith(SpringRunner.class)
@Slf4j
@EnableAutoConfiguration
public class OctopusZookeeperTest {
    private final String key = "key";
    private final String key1 = "key/key1";
    private final String key2 = "key/key1/key2";
    private final String value = "value of key";

    /**
     * 初始化几个用例可能需要用到的键值对，顺带新增和检查用例
     *
     * @throws OctopusZookeeperException failure
     */
    @Before
    public void setUp() throws OctopusZookeeperException {
        if (zookeeperOperator.nonexistent(key)) {
            zookeeperOperator.create(key, value);
        }
        if (zookeeperOperator.nonexistent(key1)) {
            String value1 = "value of key1";
            zookeeperOperator.create(key1, value1);
        }
        if (zookeeperOperator.nonexistent(key2)) {
            String value2 = "value of key2";
            zookeeperOperator.create(key2, value2);
        }
    }

    /**
     * 销毁测试用例的键值对 顺带删除和检查的用例
     *
     * @throws OctopusZookeeperException failure
     */
    @After
    public void destroy() throws OctopusZookeeperException {
        if (!zookeeperOperator.nonexistent(key)) {
            zookeeperOperator.delete(key, true);
        }
        if (!zookeeperOperator.nonexistent(key1)) {
            zookeeperOperator.delete(key1);
        }
        if (!zookeeperOperator.nonexistent(key2)) {
            zookeeperOperator.delete(key2);
        }
    }

    @Autowired
    OctopusZookeeper zookeeperOperator;

    /**
     * zk 增删改查的用例
     *
     * @throws OctopusZookeeperException failure
     */
    @Test
    public void zookeeperCurd() throws OctopusZookeeperException {
        final String update = "update for key";
        //read
        Assertions.assertThat(zookeeperOperator.get(key)).isNotNull().isEqualTo(value);
        //update
        zookeeperOperator.update(key, update);
        Assertions.assertThat(zookeeperOperator.get(key)).isNotNull().isEqualTo(update);
        //children api
        Assertions.assertThat(zookeeperOperator.children(key)).isNotEmpty();
        //delete
        zookeeperOperator.delete(key, true);
        Assertions.assertThat(zookeeperOperator.get(key)).isNull();
    }

    /**
     * watcher test
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zookeeperWatcher() throws OctopusZookeeperException {
        AtomicInteger watched = new AtomicInteger();
        //watcher
        zookeeperOperator.registerPathWatcher(key,
                (k, v) -> log.debug("Changed=====>>>{Path:{},Data:{},watched:{}}", k, v, watched.getAndIncrement()))
                .update(key, "update for zookeeper").create(key + "/key2", "key2 value").delete(key + "/key2");
        Assertions.assertThat(watched.get()).isSameAs(3);
    }


    /**
     * watcher2 test
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zookeeperWatcher2() throws OctopusZookeeperException {
        zookeeperOperator.registerPathChildrenWatcher(key,
                new ZookeeperPathChildrenWatcher<String, Serializable>() {
                    @Override
                    public void childAdd(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("childAdd=====>>>{Path:%s,Data:%s}", s, s2));
                        Assertions.assertThat(s).isNotNull().isNotBlank();
                    }

                    @Override
                    public void childRemove(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("childRemove=====>>>{Path:%s,Data:%s}", s, s2));
                        Assertions.assertThat(s).isNull();
                    }

                    @Override
                    public void childUpdate(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("childUpdate=====>>>{Path:%s,Data:%s}", s, s2));
                        Assertions.assertThat(s2).isNotNull();
                        //key updated
                        Assertions.assertThat(s2).isEqualTo("update");
                    }

                    @Override
                    public void initialized(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("initialized=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectLost(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("connectLost=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectSuspended(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("connectSuspended=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectReconnect(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("connectReconnect=====>>>{Path:%s,Data:%s}", s, s2));
                    }
                }
        ).create(key + "/seconds").update(key + "/seconds", "update").delete(key + "/seconds");
    }

    /**
     * watcher3 test
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zookeeperWatcher3() throws OctopusZookeeperException {
        zookeeperOperator.registerPathAndChildrenWatcher(key,
                new ZookeeperPathAndChildrenWatcher<String, Serializable>() {
                    @Override
                    public void pathAdd(OctopusZookeeper operator, String s, Serializable s2) {
                        log.debug("pathAdd=====>>>{Path:{},Data:{}}", s, s2);
                        Assertions.assertThat(s).isNotNull().isNotBlank();
                        //key2 created
                        Assertions.assertThat(s).isEqualTo(key2 + "/second2");
                        Assertions.assertThat(s2).isNotNull();
                        //key2 created
                        Assertions.assertThat(s2).isEqualTo("value2");
                    }

                    @Override
                    public void pathRemove(OctopusZookeeper operator, String s, Serializable s2) {
                        log.debug("pathRemove=====>>>{Path:{},Data:{}}", s, s2);
                        //key deleted
                        Assertions.assertThat(s).isNull();
                        Assertions.assertThat(s2).isNull();
                    }

                    @Override
                    public void pathUpdate(OctopusZookeeper operator, String s, Serializable s2) {
                        log.debug("pathUpdate=====>>>{Path:{},Data:{}}", s, s2);
                        //key updated
                        Assertions.assertThat(s).isNotNull();
                        Assertions.assertThat(s).isEqualTo(key);
                        Assertions.assertThat(s2).isNotNull();
                        Assertions.assertThat(s2).isEqualTo("update");
                    }

                    @Override
                    public void initialized(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("initialized=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectLost(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("connectLost=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectSuspended(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("connectSuspended=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectReconnect(OctopusZookeeper operator, String s, Serializable s2) {
                        System.out.println(String.format("connectReconnect=====>>>{Path:%s,Data:%s}", s, s2));
                    }
                }
        ).update(key, "update").create(key2 + "/second2", "value2").delete(key2 + "/second2");
    }

    /**
     * 轮询操作
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zookeeperPolling() throws OctopusZookeeperException {
        //polling stream ==>update key==>delete key1==>delete key2
        zookeeperOperator.update(key, "update").delete(key1).update(key2, "update value of key2", 10086);
        //polling result only exist key key
        Serializable val = zookeeperOperator.get(key);
        Assertions.assertThat(val).isNotNull().isEqualTo("update");
        Assertions.assertThat(zookeeperOperator.get(key1)).isNull();
        Assertions.assertThat(zookeeperOperator.get(key2)).isNotNull().isEqualTo("update value of key2");
    }

    /**
     * zk事物操作 异常回滚 正常提交
     */
    @Test(expected = OctopusZookeeperException.class)
    public void zookeeperTransact() throws OctopusZookeeperException {
        Collection<?> result = zookeeperOperator.inTransaction(o -> {
            try {
                o.create(key + "/zookeeperTransact", value).delete(key + "/zookeeperTransact")
                        .update(key, "zookeeperTransact update");
            } catch (OctopusZookeeperException ignore) {
            }
        });
        Assertions.assertThat(result).isNotNull();
        //check submit
        Assertions.assertThat(zookeeperOperator.get(key + "/zookeeperTransact")).isNull();
        Assertions.assertThat(zookeeperOperator.get(key)).isNotNull().isEqualTo("zookeeperTransact update");
        //test for rollback ...
    }

    @Test
    public void zookeeperAsync() throws Exception {
        Assertions.assertThat(this.zookeeperOperator.inAsync(action -> {
                    try {
                        action.create(key + "/zookeeperAsync")
                                .delete(key + "/zookeeperAsync")
                                .update(key, "zookeeperAsync update");
                    } catch (OctopusZookeeperException ignore) {
                    }
                }, (k, v) -> {
                    System.out.println(String.format("===>>>Thread:%s,Data:%s", Thread.currentThread().getName(), v));
                }
                , (a, v) -> System.out.println("===>>>ERROR:" + v)
        ).get(key2)).isNotNull();
    }
}
