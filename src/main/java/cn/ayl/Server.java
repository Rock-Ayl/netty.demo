package cn.ayl;

import cn.ayl.entry.RegistryEntry;
import cn.ayl.socket.server.SocketServer;

/**
 * created By Rock-Ayl on 2019-11-13
 * 🐖启动程序
 */
public class Server {

    public static void main(String[] args) {
        //扫描所有服务已存在
        RegistryEntry.scanServices();
        //启动netty监听
        new SocketServer().startup();
    }

}