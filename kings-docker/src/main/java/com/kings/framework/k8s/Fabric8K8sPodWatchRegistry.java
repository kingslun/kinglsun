package com.kings.framework.k8s;

import com.kings.framework.K8sPodListener;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.kings.framework.K8sPodListener.FALSE;
import static com.kings.framework.K8sPodListener.PodStatus.*;

/**
 * 基于Fabric8框架实现的对k8s集群下指定namespace节点下所有pod实例状态监控管理的注册中心
 * 能基于pod生命周期内各个阶段进行快速响应
 * feature:对pod宕机进行检测 发送钉钉通知
 * See <a href="http://code.aihuishou.com/fusion/ahs-nova/-/issues/4">shutdown listing</a> for a details of the feature
 *
 * @see K8sPodListener 通过对pod各阶段的状态变更抽离的监听接口 是对各阶段响应后的业务逻辑进行抽离和封装
 */
public class Fabric8K8sPodWatchRegistry {
    /**
     * 具体操作k8s集群的客户端连接工具 by Fabric8框架
     */
    private final KubernetesClient client;

    public Fabric8K8sPodWatchRegistry(KubernetesClient client) {
        Assert.notNull(client, "Add pods listener without KubernetesClient");
        this.client = client;
    }

    /**
     * pod状态判断
     * conditions 分别有四个状态 Initialized Ready ContainersReady PodScheduled
     * containerStatuses分别有两种状态 started ready
     * 如果一个为false则表示pod并非running状态
     */
    private static final Predicate<PodStatus> READY = status -> {
        List<PodCondition> conditions = status.getConditions();
        List<ContainerStatus> statuses = status.getContainerStatuses();
        for (PodCondition condition : conditions) {
            if (Objects.equals(FALSE, condition.getStatus())) {
                return false;
            }
        }
        for (ContainerStatus status1 : statuses) {
            if (Objects.equals(Boolean.FALSE, status1.getReady()) ||
                    Objects.equals(Boolean.FALSE, status1.getStarted())) {
                return false;
            }
        }
        return true;
    };

    /*
     * shutdown状态
     * 因为以下判断条件可能跟随三方组件API调整或切换导致变动 抽离出来是为了迎合上述变动而自身的最小变动
     * 1.conditions 四个状态 Initialized:true Ready:false ContainersReady:false PodScheduled:true
     * 2.containerStatuses started:false ready:false
     * 3.containerStatuses.state 有terminated属性
     */
    private static final Predicate<PodStatus> SHUTDOWN_ = status -> {
        //依据SHUTDOWN的规则他的READY为false
        if (READY.test(status)) {
            return false;
        }
        List<ContainerStatus> statuses = status.getContainerStatuses();
        for (ContainerStatus status1 : statuses) {
            if (status1.getState().getTerminated() != null) {
                return true;
            }
        }
        return false;
    };

    /**
     * pod是否Terminating状态
     * 目前fabric8检测的k8s pod Terminating共有4种状态
     * <p>
     * 其中三种为shutdown之初到彻底delete之间
     * - 分三阶段 running->ContainersNotReady
     * 一种为delete状态 不在此处验证
     * </p>
     * 他们的特点是metadata信息包含deletionTimestamp时间凭证
     * deletionGracePeriodSeconds：为具体优雅停机最大等待时间（猜测）这个值在会在最后一次置空 因此不能拿来衡量
     */
    private static final Predicate<String> TERMINATING_ = StringUtils::hasText;
    /**
     * 区分是watcher初始化还是真正的pod新建
     * 真正的新建条件
     * 1.spec未分配node name
     * 2.phase为Pending状态
     * 3.不包含状态信息和容器状态信息
     * 4.不包含如果容器IP信息
     * 5.不包含开启时间信息
     */
    private static final BiPredicate<K8sPodListener.Pod, PodStatus> ADD_RUNNING = (pod, status) ->
            StringUtils.hasText(pod.getNodeName()) ||
                    !Objects.equals(K8sPodListener.PENDING, pod.getPhase()) ||
                    StringUtils.hasText(pod.getStartTime()) ||
                    StringUtils.hasText(pod.getHostIp()) || StringUtils.hasText(pod.getPodIp()) ||
                    !CollectionUtils.isEmpty(status.getConditions()) ||
                    !CollectionUtils.isEmpty(status.getContainerStatuses());

    /**
     * 对指定namespace下的pods进行监听
     *
     * @param listener 监听者
     * @param ns       namespace
     * @author lun.wang
     * @date 2021/06/21 18:11
     * @see K8sPodListener
     * @since v1.1
     */
    public void addPodListener(@NotNull String ns, @NotNull K8sPodListener listener) {
        Assert.hasText(ns, "Add pods listener without namespace");
        Assert.notNull(listener, "Add pods listener without listener");
        Watcher<Pod> watcher = new PodWatcher(listener);
        try {
            this.client.pods().inNamespace(ns).watch(watcher);
        } catch (java.lang.Exception e) {
            listener.onException(new K8sPodListener.Exception(e));
        }
    }

    /**
     * K8s pod监听器 负责衔接fabric8框架和K8sPodListener的桥梁
     *
     * @see K8sPodListener,Watcher,Pod
     */
    private static class PodWatcher implements Watcher<Pod> {
        private final K8sPodListener listener;

        PodWatcher(K8sPodListener listener) {
            this.listener = listener;
        }

        private K8sPodListener.Pod convert(Pod pod) {
            final ObjectMeta metadata = pod.getMetadata();
            final PodStatus status = pod.getStatus();
            K8sPodListener.Pod bak = new K8sPodListener.Pod();
            bak.setName(metadata.getName());
            bak.setDeployment(metadata.getLabels().get("app"));
            bak.setLanguage(metadata.getLabels().get("language"));
            bak.setNamespace(metadata.getNamespace());
            //真正的新建是还没有分配node name的
            bak.setNodeName(pod.getSpec().getNodeName());
            bak.setHostIp(status.getHostIP());
            bak.setPodIp(status.getPodIP());
            bak.setStartTime(status.getStartTime());
            bak.setPhase(status.getPhase());
            if (!CollectionUtils.isEmpty(status.getContainerStatuses())) {
                bak.setRestartCount(status.getContainerStatuses().get(0).getRestartCount());
            }
            return bak;
        }

        @Override
        public void eventReceived(Action action, Pod pod) {
            final PodStatus status = pod.getStatus();
            K8sPodListener.Pod bak = convert(pod);
            switch (action) {
                case MODIFIED:
                    /*
                     * 区分是pod初始化后的各个准备阶段还是重启的各阶段还是删除阶段的状态变更
                     * 此阶段会存在复杂判断需要保证严谨性
                     * 1.phase一定是Pending状态的一定是还在初始化阶段
                     */
                    if (Objects.equals(K8sPodListener.PENDING, bak.getPhase())) {
                        listener.apply(bak, PENDING);
                        break;
                    }
                    //2.Running状态的会有很多场景 Pending完成及shutdown/Terminating阶段都是Running
                    if (!Objects.equals(K8sPodListener.RUNNING, bak.getPhase())) {
                        listener.apply(bak, UNKNOWN);
                        break;
                    }
                    final List<PodCondition> conditions = status.getConditions();
                    final List<ContainerStatus> containerStatuses = status.getContainerStatuses();
                    if (CollectionUtils.isEmpty(conditions) || CollectionUtils.isEmpty(containerStatuses)) {
                        //正常非pending状态不会至此
                        listener.apply(bak, UNKNOWN);
                        break;
                    }
                    if (SHUTDOWN_.test(status)) {
                        //吧shutdown摘出来
                        listener.apply(bak, SHUTDOWN);
                    } else if (TERMINATING_.test(pod.getMetadata().getDeletionTimestamp())) {
                        //吧Terminating摘出来
                        listener.apply(bak, TERMINATING);
                    } else {
                        listener.apply(bak, RUNNING);
                    }
                    break;
                case DELETED: //在删除deployment时触发 流程是先优雅停机所有节点,期间会触发多次MODIFIED 最终成功之后才会触发此入口
                    listener.apply(bak, DELETE);
                    break;
                case ADDED:
                    if (ADD_RUNNING.test(bak, status)) {
                        listener.apply(bak, RUNNING);
                    } else {
                        listener.apply(bak, CREAT);
                    }
                    break;
                case ERROR:
                    //目前未遇到过的状态 可能是pod异常时触发
                    listener.apply(bak, UNKNOWN);
                    break;
            }
        }

        @Override
        public void onClose(KubernetesClientException e) {
            listener.onException(new K8sPodListener.Exception(e));
        }
    }
}
