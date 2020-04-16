package com.github.yafeiwang124.tcp.network.server.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.yafeiwang124.tcp.network.handler.IRequestCallback;
import com.github.yafeiwang124.tcp.network.server.ITcpClient;
import com.github.yafeiwang124.tcp.protocol.MessageContext;
import com.github.yafeiwang124.tcp.protocol.codec.ProtocolDecoder;
import com.github.yafeiwang124.tcp.protocol.codec.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TcpClient implements ITcpClient {
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    private Bootstrap bootstrap;
    private EventLoopGroup loopGroup;
    private int threads;
    private String host;
    private int port;
    private Map<String, IRequestCallback> callbacks = new ConcurrentHashMap<>();
    private ChannelFuture channelFuture;

    public TcpClient(int threads, String host, int port) throws InterruptedException {
        this.threads = threads;
        this.host = host;
        this.port = port;
        init();
    }

    private void init() throws InterruptedException {
        loopGroup = new NioEventLoopGroup(threads);
        bootstrap = new Bootstrap();
        channelFuture = bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder", new ProtocolDecoder())
                                .addLast("encoder", new ProtocolEncoder())
                                .addLast("responseHandler", new ResponseHandler());
                    }
                }).connect().sync();
    }

    private class ResponseHandler extends SimpleChannelInboundHandler<MessageContext> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MessageContext msg) throws Exception {
            logger.info("收到响应，{}， {}", msg.getMessage(), JSONObject.toJSONString(msg));
            if(callbacks.containsKey(msg.getMessageId())) {
                if(msg.isSucceed()) {
                    callbacks.get(msg.getMessageId()).invoke(msg.getMessage());
                } else {
                    callbacks.get(msg.getMessageId()).onFail(msg.getExceptionMessage());
                }
                callbacks.remove(msg.getMessageId());
            }
        }
    }

    @Override
    public void tell(Object message, IRequestCallback callback) throws Exception {
        MessageContext messageContext = new MessageContext(UUID.randomUUID().toString(), message);
        if(callback != null) {
            callbacks.put(messageContext.getMessageId(), callback);
        }
        channelFuture.channel().writeAndFlush(messageContext).sync();
    }

    @Override
    public Object ask(Object request) throws Exception {
        CompletableFuture<Object> future = new CompletableFuture<>();
        IRequestCallback callback = new SynResponseCallback(future);
        tell(request, callback);
        return future.get();
    }

    private class SynResponseCallback implements IRequestCallback {
        CompletableFuture<Object> future;

        public SynResponseCallback(CompletableFuture<Object> future) {
            this.future = future;
        }

        @Override
        public void invoke(Object message) {
            future.complete(message);
        }

        @Override
        public void onFail(String exceptionMessage) {
            future.completeExceptionally(new Exception(exceptionMessage));
        }
    }

    @Override
    public Object ask(Object request, long timeout, TimeUnit unit) throws Exception {
        CompletableFuture<Object> future = new CompletableFuture<>();
        IRequestCallback callback = new SynResponseCallback(future);
        tell(request, callback);
        return future.get(timeout, unit);
    }

    @Override
    public boolean isActive() {
        return channelFuture.channel().isActive()
                || channelFuture.channel().isOpen()
                || channelFuture.channel().isRegistered();
    }

    @Override
    public void close() throws IOException {
        if(isActive()) {
            try {
                channelFuture.channel().close();
            } catch (Exception e) {

            }
        }
        loopGroup.shutdownGracefully();
    }
}
