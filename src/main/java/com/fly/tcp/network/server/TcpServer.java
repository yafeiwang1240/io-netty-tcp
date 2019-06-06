package com.fly.tcp.network.server;

import com.fly.tcp.protocol.MessageContext;
import com.fly.tcp.protocol.codec.ProtocolDecoder;
import com.fly.tcp.network.handler.IRequesthandler;
import com.fly.tcp.protocol.codec.ProtocolEncoder;
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
    private Map<Class, IRequesthandler<?, ?>> handlerMap = new ConcurrentHashMap<>();

    public TcpServer() {
        this(1240, 2, 40);
    }

    public TcpServer(int port, int parentThreads, int workerThreads) {
        this.port = port;
        this.parentGroup = new NioEventLoopGroup(parentThreads);
        this.workerGroup = new NioEventLoopGroup(workerThreads);
    }

    public TcpServer addHandler(IRequesthandler handler) {
        handlerMap.put(handler.messageType(), handler);
        return this;
    }

    public TcpServer addHandlers(List<IRequesthandler> handlers) {
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
        protected void messageReceived(ChannelHandlerContext ctx, MessageContext msg) throws Exception {

        }
    }

    public TcpServer sync() throws InterruptedException {
        channelFuture.channel().closeFuture().sync();
        return this;
    }

    public IRequesthandler getHandler(Class type) {
        if(type == null) {
            return null;
        }

        if(type == Object.class) {
            return handlerMap.get(type);
        }

        if (handlerMap.containsKey(type)) {
            return handlerMap.get(type);
        }

        IRequesthandler handler = getHandler(type.getSuperclass());
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
