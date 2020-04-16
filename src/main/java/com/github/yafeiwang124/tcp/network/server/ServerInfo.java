package com.github.yafeiwang124.tcp.network.server;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    private String ip;
    private int port;

    public ServerInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ServerInfo() {}

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
