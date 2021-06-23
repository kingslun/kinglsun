package com.kings.framework;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * <p>
 * k8s容器pod实例监听器 监听实例扩容、缩容、更新事件
 * exp: 对指定namespace下的pods进行监听
 * </p>
 *
 * @author lun.wang
 * @date 2021/6/21 5:38 下午
 * @since 1.0
 */
public interface K8sPodListener {
    /**
     * K8s虚拟机节点实例信息描述对象
     * pod描述信息
     */
    @Getter
    @Setter
    @ToString
    class Pod {
        /**
         * 实例名称
         */
        private String name;
        /**
         * 宿主机IP
         */
        private String hostIp;
        /**
         * 实例IP
         */
        private String podIp;
        /**
         * 实例开机时间
         */
        private String startTime;
        /**
         * 部署计划名称 deployment/rs name
         */
        private String deployment;
        /**
         * 所属分区 ns
         */
        private String namespace;
        /**
         * 节点名称 node name
         */
        private String nodeName;
        /**
         * 所属语言
         */
        private String language;
        /**
         * 实例状态
         */
        @NonNull
        private PodStatus status;
        /**
         * 实例重启次数
         */
        private int restartCount;
        /**
         * 容器 阶段状态
         */
        private String phase;
    }

    /**
     * 实例状态
     */
    enum PodStatus {
        /**
         * 创建
         */
        CREAT,
        /**
         * 停机
         */
        SHUTDOWN,
        /**
         * 变更
         */
        PENDING,
        /**
         * 变更
         */
        TERMINATING,
        /**
         * 正在运行
         */
        RUNNING,
        /**
         * 未知异常
         */
        UNKNOWN,
        /**
         * 删除
         */
        DELETE,
    }

    class Exception extends RuntimeException {
        public Exception(String message) {
            super(message);
        }

        public Exception(Throwable cause) {
            super(cause);
        }
    }

    default void apply(@NonNull Pod pod, @NonNull PodStatus status) {
        Assert.notNull(pod, "Apply kubernetes listener arguments pod is null");
        Assert.notNull(status, "Apply kubernetes listener arguments status is null");
        pod.setStatus(status);
        switch (status) {
            case CREAT:
                this.onPodCreating(pod);
                break;
            case DELETE:
                this.onPodDelete(pod);
                break;
            //TERMINATING PENDING状态共享
            case TERMINATING:
            case PENDING:
                this.onPodPending(pod);
                break;
            case RUNNING:
                this.onPodRunning(pod);
                break;
            case UNKNOWN:
                this.onPodUnKnown(pod);
                break;
            case SHUTDOWN:
                this.onPodShutdown(pod);
                break;
            default:
                throw new Exception("Illegal status exception @PodStatus");
        }
    }

    /**
     * ns下有新建pod
     *
     * @param pod 创建的Pod
     */
    default void onPodCreating(Pod pod) {

    }

    /**
     * ns下有pod关机
     *
     * @param pod 销毁的Pod
     */
    default void onPodShutdown(Pod pod) {

    }

    /**
     * ns下有pod移除
     *
     * @param pod 移除的Pod
     */
    default void onPodDelete(Pod pod) {

    }

    /**
     * ns下有pod发生异常
     *
     * @param pod 异常的Pod
     */
    default void onPodUnKnown(Pod pod) {

    }

    /**
     * pod运行状态 这个阶段可能会多次调用 因为watcher首次初始化或者容器准备阶段成功之后会进入运行态
     *
     * @param pod 运行的Pod
     */
    default void onPodRunning(Pod pod) {

    }

    /**
     * pod准备阶段 这个阶段可能会频繁调用 因为节点初始化和重启、停机等多个阶段都会有对应准备流程 因此此事件是共享的
     * 共享PENDING和TERMINATING两种状态
     *
     * @param pod 正在准备的pod
     * @see PodStatus#PENDING,PodStatus#TERMINATING
     */
    default void onPodPending(Pod pod) {

    }

    /**
     * 监听关闭时触发
     *
     * @param e error
     */
    default void onException(Exception e) {

    }

    /**
     * 等待状态
     */
    String PENDING = "Pending";
    /**
     * 运行状态
     */
    String RUNNING = "Running";
    /**
     * k8s pod未完成状态
     */
    String FALSE = "False";
}
