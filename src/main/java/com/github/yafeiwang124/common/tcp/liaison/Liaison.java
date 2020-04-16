package com.github.yafeiwang124.common.tcp.liaison;

import com.github.yafeiwang124.common.tcp.liaison.pool.ShareablePool;
import com.github.yafeiwang124.common.tcp.network.handler.IServerInfoHandler;
import com.github.yafeiwang124.common.tcp.network.server.ITcpClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Liaison implements Closeable {
    private String liaisonName;
    private ShareablePool<ITcpClient> clientShareablePool;

    public Liaison(String liaisonName, int threads, long expireAfter, IServerInfoHandler serverInfoHandler) {
        this.liaisonName = liaisonName;
        this.clientShareablePool = new ShareablePool(expireAfter, new TcpClientManager(threads, liaisonName, serverInfoHandler), ITcpClient.class);
    }

    public  <T> T ask(Object request, long timeout, TimeUnit unit) throws Exception {
        try (ITcpClient client = clientShareablePool.getConnection()){
            Object result = client.ask(request, timeout, unit);
            if (result == null) {
                return null;
            }
            return (T) result;
        }
    }

    public <T> T ask(Object request) throws Exception {
        try (ITcpClient client = clientShareablePool.getConnection()){
            Object result = client.ask(request);
            if (result == null) {
                return null;
            }
            return (T) result;
        }
    }

    @Override
    public void close() throws IOException {
        if (clientShareablePool != null) {
            clientShareablePool.close();
        }
    }

}
