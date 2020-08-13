package com.kings.schedule.common.netty.serialize;

import com.kings.schedule.common.netty.message.SchedulerRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 4:16 下午
 * @email lun.kings@zatech.com
 * @since v1.0.0
 */
public class SchedulerRequestEncoder extends MessageToByteEncoder<SchedulerRequest> {

    private final AbstractKryoSchedulerMessageSerializer<SchedulerRequest> requestSerializer =
            new AbstractKryoSchedulerMessageSerializer<SchedulerRequest>(SchedulerRequest.class) {
            };

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, SchedulerRequest o, ByteBuf out)
            throws Exception {
        byte[] data = requestSerializer.serialize(o);
        out.writeInt(data.length);
        //先将消息长度写入，也就是消息头
        out.writeBytes(data);
        //消息体中包含我们要发送的数据
    }
}
