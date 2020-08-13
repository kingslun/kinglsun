package com.kings.scheduler.manager.server;

import com.kings.schedule.common.netty.message.SchedulerRequest;
import com.kings.schedule.common.netty.message.SchedulerResponse;
import com.kings.schedule.common.netty.serialize.SchedulerDecoder;
import com.kings.schedule.common.netty.serialize.SchedulerRequestEncoder;
import com.kings.schedule.common.netty.serialize.SchedulerResponseEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 4:06 下午
 * @email lun.kings@zatech.com
 * @since v1.0.0
 */
public class NettyServer {
    public static void main(String[] args) {
        NettyServer server = new NettyServer();
        try {
            server.bind(8000);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void bind(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //bossGroup就是parentGroup，是负责处理TCP/IP连接的
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //workerGroup就是childGroup,是负责处理Channel(通道)的I/O事件
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                //初始化服务端可连接队列,指定了队列的大小128
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //保持长连接
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    // 绑定客户端连接时候触发操作
                    @Override
                    protected void initChannel(SocketChannel sh) throws Exception {
                        sh.pipeline()
                                .addLast(new SchedulerRequestEncoder())
                                .addLast(new SchedulerResponseEncoder())
                                //解码request
                                .addLast(new SchedulerDecoder(SchedulerRequest.class))
                                .addLast(new SchedulerDecoder(SchedulerResponse.class))
                                //编码response
                                .addLast(new ServerSchedulerRequestHandler())
                                .addLast(new ServerHandler());
                        //使用ServerHandler类来处理接收到的消息
                    }
                });
        //绑定监听端口，调用sync同步阻塞方法等待绑定操作完
        ChannelFuture future = sb.bind(port).sync();
        if (future.isSuccess()) {
            System.out.println("服务端启动成功");
        } else {
            //关闭线程组
            future.cause().printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        //成功绑定到端口之后,给channel增加一个 管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程。
        future.channel().closeFuture().sync();
    }

    @Slf4j
    static class ServerSchedulerRequestHandler extends SimpleChannelInboundHandler<SchedulerRequest> {
        @Override
        protected void messageReceived(ChannelHandlerContext ctx, SchedulerRequest o)
                throws Exception {
            if (log.isDebugEnabled()) {
                log.debug("server received message:{}", o);
            }
            SchedulerResponse response = new SchedulerResponse();
            ctx.writeAndFlush(response);
        }
    }
}
