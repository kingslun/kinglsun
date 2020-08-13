package com.kings.scheduler.client;

import com.kings.schedule.common.netty.message.SchedulerRequest;
import com.kings.schedule.common.netty.message.SchedulerResponse;
import com.kings.schedule.common.netty.serialize.SchedulerDecoder;
import com.kings.schedule.common.netty.serialize.SchedulerRequestEncoder;
import com.kings.schedule.common.netty.serialize.SchedulerResponseEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 5:39 下午
 * @email lun.kings@zatech.com
 * @since
 */
public class NettyClient {
    public static void main(String[] args) {
        NettyClient client = new NettyClient("localhost", 8000);
        try {
            client.start();
            SchedulerRequest request = new SchedulerRequest();
            request.setId(100000000);
            request.setMsg("client connect");
            client.getChannel().writeAndFlush(request);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private final String host;
    private final int port;
    @Getter
    private Channel channel;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<SocketChannel>() {
                    // 绑定连接初始化器
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("正在连接中...");
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new SchedulerRequestEncoder());
                        pipeline.addLast(new SchedulerResponseEncoder());
                        //编码request
                        pipeline.addLast(new SchedulerDecoder(SchedulerRequest.class));
                        pipeline.addLast(new SchedulerDecoder(SchedulerResponse.class));
                        //解码response
                        pipeline.addLast(new ClientMessage());
                        //客户端处理类
                    }
                });
        //发起异步连接请求，绑定连接端口和host信息
        final ChannelFuture future = b.connect(host, port).sync();
        future.addListener((ChannelFutureListener) arg0 -> {
            if (future.isSuccess()) {
                System.out.println("连接服务器成功");

            } else {
                System.out.println("连接服务器失败");
                future.cause().printStackTrace();
                group.shutdownGracefully(); //关闭线程组
            }
        });
        this.channel = future.channel();
    }

    @Slf4j
    static class ClientMessage extends SimpleChannelInboundHandler<SchedulerResponse> {
        @Override
        protected void messageReceived(ChannelHandlerContext channelHandlerContext, SchedulerResponse schedulerResponse)
                throws Exception {
            if (log.isDebugEnabled()) {
                log.debug("client received message:{}", schedulerResponse);
            }
        }
    }
}
