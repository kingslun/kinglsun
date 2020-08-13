package com.kings.scheduler.manager.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 5:19 下午
 * @email lun.kings@zatech.com
 * @since
 */
@Slf4j
public class ServerHandler extends ChannelHandlerAdapter {
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        log.debug("client connected,remote:{},local:{},promise:{}", remoteAddress, localAddress, promise);
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("client disconnected,promise:{}", promise);
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("client closed,promise:{}", promise);
        super.close(ctx, promise);
    }
}
