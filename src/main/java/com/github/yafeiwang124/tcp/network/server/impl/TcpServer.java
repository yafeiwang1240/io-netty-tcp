package com.github.yafeiwang124.tcp.network.server.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.yafeiwang124.tcp.protocol.MessageContext;
import com.github.yafeiwang124.tcp.protocol.VoidProtocol;
import com.github.yafeiwang124.tcp.protocol.codec.ProtocolDecoder;
import com.github.yafeiwang124.tcp.network.handler.IRequestHandler;
import com.github.yafeiwang124.tcp.protocol.codec.ProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TcpServer implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
    private EventLoopGroup parentGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    private int port;
    private ChannelFuture channelFuture;
    private Map<Class, IRequestHandler<?, ?>> handlerMap = new ConcurrentHashMap<>();

    public TcpServer() {
        this(1240, 2, 40);
    }

    public TcpServer(int port, int parentThreads, int workerThreads) {
        this.port = port;
        this.parentGroup = new NioEventLoopGroup(parentThreads);
        this.workerGroup = new NioEventLoopGroup(workerThreads);
    }

    public TcpServer addHandler(IRequestHandler handler) {
        handlerMap.put(handler.messageType(), handler);
        return this;
    }

    public TcpServer addHandlers(List<IRequestHandler> handlers) {
        handlers.forEach(item -> {
            addHandler(item);
        });
        return this;
    }

    public TcpServer start() throws InterruptedException {
        serverBootstrap = new ServerBootstrap();
        channelFuture = serverBootstrap.group(parentGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new ProtocolDecoder())
                                .addLast("encoder", new ProtocolEncoder())
                                .addLast("handlerAdapter",  new RequestMappingHandler());
                    }
                }).bind(port).sync();
        if(port == 0) {
            InetSocketAddress address = (InetSocketAddress) channelFuture.channel().localAddress();
            port = address.getPort();
        }
        return this;
    }

    @ChannelHandler.Sharable
    private class RequestMappingHandler extends SimpleChannelInboundHandler<MessageContext> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MessageContext msg) throws Exception {
            Class typeClass = msg.getMessage().getClass();
            IRequestHandler handler = getHandler(typeClass);
            logger.info("收到请求，类型{}，内容{}", typeClass, JSONObject.toJSONString(msg));
            if(handler != null) {
                try {
                    Object result = handler.handle(msg.getMessage());
                    MessageContext context = new MessageContext(msg.getMessageId(), result);
                    logger.info("返回结果：" + JSONObject.toJSONString(context));
                    ctx.channel().writeAndFlush(context);
                } catch (Exception e) {
                    logger.error("请求处理异常！" + JSONObject.toJSONString(msg.getMessage()), e);
                    MessageContext context = new MessageContext(msg.getMessageId(), VoidProtocol.getInstance());
                    context.setSucceed(false);
                    context.setExceptionMessage(e.getMessage());
                    ctx.channel().writeAndFlush(context);
                }
            } else {
                MessageContext context = new MessageContext(msg.getMessageId(), VoidProtocol.getInstance());
                context.setSucceed(false);
                context.setExceptionMessage("不支持的消息类型！");
                logger.info("返回结果：" + JSONObject.toJSONString(context));
                ctx.channel().writeAndFlush(context);
            }
        }
    }

    public TcpServer sync() throws InterruptedException {
        channelFuture.channel().closeFuture().sync();
        return this;
    }

    public IRequestHandler getHandler(Class type) {
        if(type == null) {
            return null;
        }

        if(type == Object.class) {
            return handlerMap.get(type);
        }

        if (handlerMap.containsKey(type)) {
            return handlerMap.get(type);
        }

        IRequestHandler handler = getHandler(type.getSuperclass());
        if(handler != null) {
            return handler;
        }
        Class<?>[] types = type.getInterfaces();
        for(Class _type : types) {
            handler = getHandler(_type);
            if(handler != null) {
                return handler;
            }
        }
        return null;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void close() throws IOException {
        parentGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
