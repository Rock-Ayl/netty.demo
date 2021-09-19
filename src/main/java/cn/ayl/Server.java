package cn.ayl;

import cn.ayl.pojo.Registry;
import cn.ayl.common.job.Scheduler;
import cn.ayl.socket.server.ServerSocket;

/**
 * created By Rock-Ayl on 2019-11-13
 * 🐖启动程序
 */
public class Server {

    public static void main(String[] args) {
        //启动定时器线程
        Scheduler.startup();
        //扫描所有服务已存在
        Registry.scanServices();
        //扫描之后,可以启动netty监听
        ServerSocket.SocketServer.startup();
    }

}