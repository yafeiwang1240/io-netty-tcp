package com.githup.yafeiwang124.common.tcp.protocol.codec;

import com.githup.yafeiwang1240.obrien.lang.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ProtocolDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() > 4) {
            byte[] byteHeader = new byte[4];
            in.readBytes(byteHeader);
            int length = Bytes.toInt(byteHeader);
            if (in.readableBytes() < length) {
                in.resetReaderIndex();
                return;
            }
            byte[] body = new byte[length];
            in.readBytes(body);
            out.add(FstSerializer.decode(body));
            return;
        }
    }
}
