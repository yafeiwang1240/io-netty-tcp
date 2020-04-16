### 基于io-netty架构的通讯服务器

##### 使用示例

```java
package com.github.yafeiwang124.tcp;

import com.github.yafeiwang124.tcp.liaison.Liaison;
import com.github.yafeiwang124.tcp.network.handler.IRequestHandler;
import com.github.yafeiwang124.tcp.network.server.ServerInfo;
import com.github.yafeiwang124.tcp.network.server.impl.TcpServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );
        TcpServer tcpServer = new TcpServer();
        tcpServer.addHandler(new JobConfigHandler());
        TcpServerThread tcpServerThread = new TcpServerThread(tcpServer);
        new Thread(tcpServerThread).start();
        Liaison liaison = new Liaison("tcp", 1, 60000, () -> {
            List<ServerInfo> serverInfoList = new ArrayList<>();
            serverInfoList.add(new ServerInfo("127.0.0.1", 1240));
            return serverInfoList;
        });
        JobConfig config = new JobConfig();
        config.setId(10L);
        config.setMsg("hhhhhhhhhh");
        String str = liaison.ask(config);
        System.out.println(str);
        liaison.close();
    }

    public static class JobConfigHandler implements IRequestHandler<JobConfig, String> {

        @Override
        public Class<JobConfig> messageType() {
            return JobConfig.class;
        }

        @Override
        public String handle(JobConfig message) throws Exception {
            System.out.println(message.getMsg());
            return "01 01 我是02";
        }
    }

    public static class JobConfig implements Serializable {
        private Long id;
        private String msg;
        public JobConfig(){

        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public static class TcpServerThread implements Runnable {
        private TcpServer tcpServer;
        public TcpServerThread(TcpServer tcpServer) {
            this.tcpServer = tcpServer;
        }
        @Override
        public void run() {
            try {
                tcpServer.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
 
```

