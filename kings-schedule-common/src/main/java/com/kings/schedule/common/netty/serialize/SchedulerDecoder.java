package com.kings.schedule.common.netty.serialize;

import com.kings.schedule.common.netty.message.SchedulerRequest;
import com.kings.schedule.common.netty.message.SchedulerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

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
public class SchedulerDecoder extends ByteToMessageDecoder {
    private static final int LEN = 4;
    /**
     * 目标对象类型进行解码
     */
    private final Class<?> target;
    private final AbstractKryoSchedulerMessageSerializer<SchedulerRequest> requestSerializer =
            new AbstractKryoSchedulerMessageSerializer<SchedulerRequest>(SchedulerRequest.class) {
            };
    private final AbstractKryoSchedulerMessageSerializer<SchedulerResponse> responseSerializer =
            new AbstractKryoSchedulerMessageSerializer<SchedulerResponse>(SchedulerResponse.class) {
            };

    public SchedulerDecoder(Class<?> target) {
        this.target = target;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        if (in.readableBytes() < LEN) {
            //不够长度丢弃
            return;
        }
        in.markReaderIndex();
        //标记一下当前的readIndex的位置
        int dataLength = in.readInt();
        // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
        if (in.readableBytes() < dataLength) {
            //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        if (target == SchedulerResponse.class) {
            out.add(responseSerializer.deserialize(data));
        }
        if (target == SchedulerRequest.class) {
            out.add(requestSerializer.deserialize(data));
        }
    }
}
