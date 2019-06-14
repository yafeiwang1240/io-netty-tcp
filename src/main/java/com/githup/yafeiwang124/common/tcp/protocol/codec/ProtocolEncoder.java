package com.githup.yafeiwang124.common.tcp.protocol.codec;

import com.githup.yafeiwang124.common.tcp.protocol.MessageContext;
import com.githup.yafeiwang124.common.tcp.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtocolEncoder extends MessageToByteEncoder<MessageContext> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageContext msg, ByteBuf out) throws Exception {
        byte[] body = FstSerializer.encode(msg);
        byte[] header = ByteUtils.toBytes(body.length);
        out.writeBytes(header);
        out.writeBytes(body);
    }
}