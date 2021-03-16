package com.kings.framework;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.BasicExecutor;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadFactory;

public class DistributorTest {
    public static void main(String[] args) throws Exception {
        // 队列中的元素
        @Getter
        @Setter
        class Element {
            private int value;
        }

        // 生产者的线程工厂
        ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");

        // RingBuffer生产工厂,初始化RingBuffer的时候使用
        EventFactory<Element> factory = Element::new;

        // 处理Event的handler
        EventHandler<Element> handler =
                (element, sequence, endOfBatch) -> System.out.println("Element: " + element.getValue());

        // 阻塞策略
        BlockingWaitStrategy strategy = new BlockingWaitStrategy();

        // 指定RingBuffer的大小
        int bufferSize = 16;

        // 创建disruptor，采用单生产者模式
        Disruptor<Element> disruptor =
                new Disruptor<>(factory, bufferSize, threadFactory, ProducerType.SINGLE, strategy);

        // 设置EventHandler
        disruptor.handleEventsWith(handler);

        // 启动disruptor的线程
        disruptor.start();

        RingBuffer<Element> ringBuffer = disruptor.getRingBuffer();

        for (int l = 0; true; l++) {
            // 获取下一个可用位置的下标
            long sequence = ringBuffer.next();
            try {
                // 返回可用位置的元素
                Element event = ringBuffer.get(sequence);
                // 设置该位置元素的值
                event.setValue(l);
            } finally {
                ringBuffer.publish(sequence);
            }
            Thread.sleep(10);
        }
    }

    @Test
    public void executors() {
        final BasicExecutor executors = new BasicExecutor((r) -> new Thread(r, "Executors-"));
        executors.execute(new Thread("inner"));
        System.out.println(executors);
    }

    @Test
    public void threadMXBean() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        System.out.println(threadMXBean.getCurrentThreadCpuTime());
    }
}
