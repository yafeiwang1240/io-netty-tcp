package com.github.yafeiwang124.common.tcp;

import com.github.yafeiwang124.common.tcp.network.handler.IRequestHandler;
import com.github.yafeiwang124.common.tcp.network.server.ITcpClient;
import com.github.yafeiwang124.common.tcp.network.server.TcpClient;
import com.github.yafeiwang124.common.tcp.network.server.TcpServer;

import java.io.Serializable;

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
        ITcpClient tcpClient = new TcpClient(1, "10.30.38.134", 1240);
        JobConfig config = new JobConfig();
        config.setId(10L);
        config.setMsg("hhhhhhhhhh");
        System.out.println(tcpClient.ask(config));
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
