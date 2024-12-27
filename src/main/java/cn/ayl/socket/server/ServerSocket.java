package cn.ayl.socket.server;

import cn.ayl.config.Const;
import cn.ayl.socket.decoder.ProtocolDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-11-4
 * netty服务端通信
 */
public enum ServerSocket {

    //单例
    SocketServer;

    protected static Logger logger = LoggerFactory.getLogger(ServerSocket.class);

    //负责接收客户端到端口的请求并交给 workerGroup,是个死循环
    private EventLoopGroup bossGroup;
    //从boss手里获得服务器的请求并处理,是个死循环
    private EventLoopGroup workerGroup;
    //通道,用来优化关闭服务端
    private Channel channel;

    /**
     * 创建并启动netty
     */
    public void startup() {

        /**
         * boss、worker
         */

        //cpu数
        int cpu = Runtime.getRuntime().availableProcessors();
        //初始化boss和worker,指定线程数( 相当于指定 网络模型 )
        this.bossGroup = new NioEventLoopGroup(cpu);
        this.workerGroup = new NioEventLoopGroup(cpu * 2);

        /**
         * 服务引导类
         */

        try {

            //用ServerBootstrap创建Server
            ServerBootstrap bootstrap = new ServerBootstrap()
                    //将boss和工作者放入(相当于指定 网络模型 )
                    .group(this.bossGroup, this.workerGroup)
                    //打印日志(非必选)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    /**
                     * 指定IO模型(其实就是指定 客户端模型 还是 服务端模型)
                     * {@link NioServerSocketChannel} 是服务端模型
                     * {@link io.netty.channel.socket.nio.NioSocketChannel} 是客户端模型
                     */
                    .channel(NioServerSocketChannel.class)
                    /**
                     * 你可以给Channel配置特有的参数。
                     * 这里我们写的是 TCP/IP 服务器，所以可以配置一些 socket 选项，例如 tcpNoDeply 和 keepAlive。
                     * 请参考ChannelOption和ChannelConfig文档来获取更多可用的 Channel 配置选项，并对此有个大概的了解。
                     * BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                     */
                    .option(ChannelOption.SO_BACKLOG, 1024)
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
                    //优化:启用 PooledByteBufAllocator 以提高内存分配效率
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            /**
             * 这里的 handler 会被用来处理新接收的Channel。
             * ChannelInitializer是一个特殊的 handler，
             * 帮助开发者配置Channel，而多数情况下你会配置Channel下的ChannelPipeline，
             * 往 pipeline 添加一些 handler (例如DiscardServerHandler) 从而实现你的应用逻辑。
             * 当你的应用变得复杂，你可能会向 pipeline 添加更多的 handler，并把这里的匿名类抽取出来作为一个单独的类。
             */
            bootstrap.childHandler(new ProtocolDecoder());
            //绑定服务器监听端口
            ChannelFuture channelFuture = bootstrap.bind(Const.SocketPort).sync();
            logger.info(">>>>>> Netty Socket server started >>>>>>");
            /**
             * Wait until the server socket is closed.(等待，直到服务器套接字关闭)
             * In this example, this does not happen, but you can do that to gracefully(在本例中，这种情况不会发生，但是您可以优雅地这样做)
             * shut down your server.(关闭你的服务)
             */
            this.channel = channelFuture.channel();
            //阻塞等待直到服务器Channel关闭(closeFuture()方法获取Channel 的CloseFuture对象,然后调用sync()方法)
            this.channel.closeFuture().sync();
        } catch (Exception e) {
            logger.error("Socket startup Error, maybe Address already in use ", e);
            //直接关闭
            System.exit(-1);
        } finally {
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();
        }
    }

    /**
     * 关闭
     */
    public void destroy() {
        if (this.channel != null) {
            this.channel.close();
        }
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        logger.info("ServerSocket Destroy:" + Const.SocketPort);
    }

}
