package com.github.yafeiwang124.common.tcp.liaison;

import com.github.yafeiwang124.common.tcp.liaison.pool.ConnectionManager;
import com.github.yafeiwang124.common.tcp.network.handler.IServerInfoHandler;
import com.github.yafeiwang124.common.tcp.network.server.ITcpClient;
import com.github.yafeiwang124.common.tcp.network.server.ServerInfo;
import com.github.yafeiwang124.common.tcp.network.server.impl.TcpClient;

import java.io.IOException;
import java.util.List;

public class TcpClientManager implements ConnectionManager<ITcpClient> {

    private int initThread;
    private String shuffleCode;
    private IServerInfoHandler serverInfoHandler;

    public TcpClientManager(int initThread, String shuffleCode, IServerInfoHandler serverInfoHandler) {
        this.initThread = initThread;
        this.shuffleCode = shuffleCode;
        this.serverInfoHandler = serverInfoHandler;
    }

    @Override
    public ITcpClient build() throws Exception {
        try {
            List<ServerInfo> serverInfos = serverInfoHandler.getServerInfo();
            if (serverInfos == null || serverInfos.size() <= 0) {
                throw new Exception("没有可用的服务");
            }
            int hashCode = shuffleCode.hashCode();
            ServerInfo serverInfo = serverInfos.get(hashCode % serverInfos.size());
            TcpClient client = new TcpClient(initThread, serverInfo.getIp(), serverInfo.getPort());
            return client;
        } finally {

        }
    }

    @Override
    public boolean isValid(ITcpClient connection) {
        return connection.isActive();
    }

    @Override
    public void release(ITcpClient connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {

            }
        }
    }
}
