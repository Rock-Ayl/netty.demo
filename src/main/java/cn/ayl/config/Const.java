package cn.ayl.config;

/**
 * created by Rock-Ayl 2019-11-5
 * 常量
 */
public class Const {

    //SocketPort
    public static int SocketPort = 8888;
    //http请求聚合字节最大长度
    public static int MaxContentLength = 65535;
    //WebSocket读空闲时间闲置/秒
    public static int ReaderIdleTimeSeconds = 8;
    //WebSocket写空闲时间闲置/秒
    public static int WriterIdleTimeSeconds = 10;
    //WebSocket所有空闲时间闲置/秒
    public static int AllIdleTimeSeconds = 12;
    public static String WebSocketPath = "/WebSocket";
    //BACKLOG值用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
    public static int ChannelOptionSoBacklogValue = 1024;

}
