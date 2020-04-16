package com.github.yafeiwang124.tcp.protocol.codec;

import com.github.yafeiwang124.tcp.protocol.MessageContext;
import com.github.yafeiwang1240.obrien.lang.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtocolEncoder extends MessageToByteEncoder<MessageContext> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageContext msg, ByteBuf out) throws Exception {
        byte[] body = FstSerializer.encode(msg);
        byte[] header = Bytes.toBytes(body.length);
        out.writeBytes(header);
        out.writeBytes(body);
    }
}
