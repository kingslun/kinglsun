package local.tets;

import com.zatech.octopus.component.distributed.zookeeper.NodeMode;
import com.zatech.octopus.component.distributed.zookeeper.OctopusZookeeper;
import com.zatech.octopus.component.distributed.zookeeper.ZookeeperPathAndChildrenWatcher;
import com.zatech.octopus.component.distributed.zookeeper.ZookeeperPathChildrenWatcher;
import com.zatech.octopus.component.distributed.zookeeper.exception.OctopusZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;

@SpringBootTest(classes = LocalDemoApplicationTests.class)
@RunWith(SpringRunner.class)
@Slf4j
@EnableAutoConfiguration
@TestPropertySource("classpath:application-zk.properties")
public class LocalDemoApplicationTests {

    @Autowired
    private Environment environment;
    @Autowired
    OctopusZookeeper zookeeperOperator;

    @Test
    public void zkLeader() {
    }

    @Test
    public void zkOperate() throws OctopusZookeeperException {
        String key = "first/second/third",
                update = "update for value with zookeeper";
        Serializable value = "hello zookeeper 3";
        //exist -> create and get unit
        if (zookeeperOperator.nonexistent(key)) {
            zookeeperOperator.create(key, value);
        } else {
            value = zookeeperOperator.get(key);
        }
        assert zookeeperOperator.get(key).equals(value) : "not that";
        //update
        zookeeperOperator.update(key, update);
        assert zookeeperOperator.get(key).equals(update) : "not that for update";
        //delete
        zookeeperOperator.delete("first", true);
        assert zookeeperOperator.get(key) == null : "delete failed";
    }

    /**
     * watcher test
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zkWatcher() throws OctopusZookeeperException {
        String key = "key", value = "hello zookeeper";
        if (zookeeperOperator.nonexistent(key)) {
            zookeeperOperator.create(key, value);
        }
        zookeeperOperator.registerPathWatcher(key, (k, v) ->
                System.out.println(String.format("Changed=====>>>{Path:%s,Data:%s}", k, v))
        );
        //此时上面应该能监听到才对 且打印key和update value
        zookeeperOperator.update(key, "update for zookeeper");
        zookeeperOperator.create(key + "/key2", "key2 value");
        //上面应该任然能监听到 且打印key和空value
        zookeeperOperator.delete(key, true);
    }


    /**
     * watcher2 test
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zkWatcher2() throws OctopusZookeeperException {
        String key = "key", value = "hello zookeeper";
        if (zookeeperOperator.nonexistent(key)) {
            zookeeperOperator.create(key, value);
        }
        zookeeperOperator.registerPathChildrenWatcher(key,
                new ZookeeperPathChildrenWatcher<String, Serializable>() {
                    @Override
                    public void childAdd(OctopusZookeeper operator, String s,
                                         Serializable s2) {
                        System.out.println(String.format(
                                "childAdd=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void childRemove(OctopusZookeeper operator, String s,
                                            Serializable s2) {
                        System.out.println(String.format(
                                "childRemove=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void childUpdate(OctopusZookeeper operator, String s,
                                            Serializable s2) {
                        System.out.println(String.format(
                                "childUpdate=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void initialized(OctopusZookeeper operator, String s,
                                            Serializable s2) {
                        System.out.println(String.format(
                                "initialized=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectLost(OctopusZookeeper operator, String s,
                                            Serializable s2) {
                        System.out.println(String.format(
                                "connectLost=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectSuspended(OctopusZookeeper operator,
                                                 String s, Serializable s2) {
                        System.out.println(String.format(
                                "connectSuspended=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectReconnect(OctopusZookeeper operator,
                                                 String s, Serializable s2) {
                        System.out.println(String.format(
                                "connectReconnect=====>>>{Path:%s,Data:%s}", s, s2));
                    }
                }
        );
        //此时上面应该能监听到才对 且打印key和update value
        zookeeperOperator.update(key, "update for zookeeper");
        zookeeperOperator.create(key + "/key2", "key child value");
        //上面应该任然能监听到 且打印key和空value
        zookeeperOperator.delete(key, true);
    }

    /**
     * watcher3 test
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zkWatcher3() throws OctopusZookeeperException {
        String key = "key", value = "hello zookeeper";
        if (zookeeperOperator.nonexistent(key)) {
            zookeeperOperator.create(key, value);
        }
        zookeeperOperator.registerPathAndChildrenWatcher(key,
                new ZookeeperPathAndChildrenWatcher<String, Serializable>() {
                    @Override
                    public void pathAdd(OctopusZookeeper operator, String s,
                                        Serializable s2) {
                        log.debug("pathAdd=====>>>{Path:{},Data:{}}", s, s2);
                    }

                    @Override
                    public void pathRemove(OctopusZookeeper operator, String s,
                                           Serializable s2) {
                        log.debug("pathRemove=====>>>{Path:{},Data:{}}", s, s2);

                    }

                    @Override
                    public void pathUpdate(OctopusZookeeper operator, String s,
                                           Serializable s2) {
                        log.debug("pathUpdate=====>>>{Path:{},Data:{}}", s, s2);
                    }

                    @Override
                    public void initialized(OctopusZookeeper operator, String s,
                                            Serializable s2) {
                        System.out.println(String.format(
                                "initialized=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectLost(OctopusZookeeper operator, String s,
                                            Serializable s2) {
                        System.out.println(String.format(
                                "connectLost=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectSuspended(OctopusZookeeper operator,
                                                 String s, Serializable s2) {
                        System.out.println(String.format(
                                "connectSuspended=====>>>{Path:%s,Data:%s}", s, s2));
                    }

                    @Override
                    public void connectReconnect(OctopusZookeeper operator,
                                                 String s, Serializable s2) {
                        System.out.println(String.format(
                                "connectReconnect=====>>>{Path:%s,Data:%s}", s, s2));
                    }
                }
        );
        //此时上面应该能监听到才对 且打印key和update value
        zookeeperOperator.update(key, "update for zookeeper");
        zookeeperOperator.create(key + "/key2", "key child value");
        //上面应该任然能监听到 且打印key和空value
        zookeeperOperator.delete(key, true);
    }

    /**
     * poll ope
     *
     * @throws OctopusZookeeperException failed
     */
    @Test
    public void zkPollOpe() throws OctopusZookeeperException {
        String key = "key", key1 = "key1", key2 = "key2", value = "value of key", update =
                "update value";
        zookeeperOperator.create(key, value).create(key1).delete(key1).update(key, update).create(
                key2).delete(key2);
    }

    @Test
    public void zkTransactionOpe() throws OctopusZookeeperException {
        String key = "key", key1 = "key1", key2 = "key2", value = "value of key", update =
                "update value";
        zookeeperOperator.inTransaction(o -> {
            try {
                o.create(key1)
                        .create(key, value)
                        .delete(key1)
                        .update(key, update)
                        .create(key2)
                        .update(key2, update);
            } catch (OctopusZookeeperException ignore) {
            }
        }).forEach(System.out::println);
    }

    @Test
    public void zkAsyncOpe() throws Exception {
        String key = "/key", key1 = "/key1", value = "value", update = "update value";
        this.zookeeperOperator.inAsync(action -> {
                    try {
                        action.create(key, value, NodeMode.EPHEMERAL).create(key1,
                                value, NodeMode.EPHEMERAL).update(key, update).delete(key1);
                    } catch (OctopusZookeeperException e) {
                        assert true : "cause error for async operate";
                    }
                }, (a, b) -> System.out.println(String.format("===>>>Thread:%s,Data:%s",
                Thread.currentThread().getName(), b))
                , (a, v) -> System.out.println("===>>>ERROR:" + v)
        );
    }
}
