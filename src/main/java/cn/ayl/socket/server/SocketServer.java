package cn.ayl.socket.server;

import cn.ayl.socket.handler.HttpHandler;
import cn.ayl.socket.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-11-4
 * 通信服务
 */
public class SocketServer {

    protected static Logger logger = LoggerFactory.getLogger(SocketServer.class);

    /**
     * 创建一个默认配置的HttpServerBootstrap
     *
     * @param bossGroup   netty-boss
     * @param workerGroup netty-work-IO
     * @return
     */
    public static ServerBootstrap createDefaultHttpServerBootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        return new ServerBootstrap()
                /**
                 * 组装Boss和IO
                 */
                .group(bossGroup, workerGroup)
                /**
                 * 这里我们指定NioServerSocketChannel类，用来初始化一个新的Channel去接收到达的connection。
                 */
                .channel(NioServerSocketChannel.class)
                /**
                 * 你可以给Channel配置特有的参数。
                 * 这里我们写的是 TCP/IP 服务器，所以可以配置一些 socket 选项，例如 tcpNoDeply 和 keepAlive。
                 * 请参考ChannelOption和ChannelConfig文档来获取更多可用的 Channel 配置选项，并对此有个大概的了解。
                 */
                .option(ChannelOption.SO_BACKLOG, 128)
                /**
                 * 注意到option()和childOption()了吗？
                 * option()用来配置NioServerSocketChannel(负责接收到来的connection)，
                 * 而childOption()是用来配置被ServerChannel(这里是NioServerSocketChannel) 所接收的Channel
                 *
                 * ChannelOption.SO_KEEPALIVE表示是否开启TCP底层心跳机制,true为开启
                 * ChannelOption.SO_REUSEADDR表示端口释放后立即就可以被再次使用,因为一般来说,一个端口释放后会等待两分钟之后才能再被使用
                 * ChannelOption.TCP_NODELAY表示是否开始Nagle算法,true表示关闭,false表示开启,通俗地说,如果要求高实时性,有数据发送时就马上发送,就关闭,如果需要减少发送次数减少网络交互就开启
                 */
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    /**
     * 创建一个默认配置的WebSocketServerBootstrap
     *
     * @param bossGroup   netty-boss
     * @param workerGroup netty-work-IO
     * @return
     */
    public static ServerBootstrap createDefaultWebSocketServerBootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    public void startup() {
        try {
            //启动Http
            //new HttpHandler().start();
            //启动WebSocket
            new WebSocketHandler().start();
        } catch (Exception e) {
            logger.error("Run Socket Fail!");
        }
    }

    public static void main(String[] args) {
        logger.info("开始");
        new SocketServer().startup();
    }

    public void stop() {

    }


}
