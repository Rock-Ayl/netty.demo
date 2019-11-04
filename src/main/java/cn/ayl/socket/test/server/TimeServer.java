package cn.ayl.socket.test.server;

import cn.ayl.socket.server.SocketServer;
import cn.ayl.socket.test.handler.TimeServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 * 启动处理程序
 */
public class TimeServer {

    public void run() throws Exception {

        /**
         * NioEventLoopGroup是一个处理I/O操作的事件循环器 (其实是个线程池)。
         * netty为不同类型的传输协议提供了多种NioEventLoopGroup的实现。
         * 在本例中我们要实现一个服务端应用，并使用了两个NioEventLoopGroup。
         * 第一个通常被称为boss，负责接收已到达的 connection。
         * 第二个被称作 worker，当 boss 接收到 connection 并把它注册到 worker 后，worker 就可以处理 connection 上的数据通信。
         * 要创建多少个线程，这些线程如何匹配到Channel上会随着EventLoopGroup实现的不同而改变，或者你可以通过构造器去配置他们。
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /**
             * ServerBootstrap是用来搭建 server 的协助类。
             * 你也可以直接使用Channel搭建 server，然而这样做步骤冗长，不是一个好的实践，大多数情况下建议使用ServerBootstrap。
             */
            ServerBootstrap bootstrap = SocketServer.createDefaultServerBootstrap(bossGroup, workerGroup);
            /**
             * 这里的 handler 会被用来处理新接收的Channel。
             * ChannelInitializer是一个特殊的 handler，
             * 帮助开发者配置Channel，而多数情况下你会配置Channel下的ChannelPipeline，
             * 往 pipeline 添加一些 handler (例如DiscardServerHandler) 从而实现你的应用逻辑。
             * 当你的应用变得复杂，你可能会向 pipeline 添加更多的 handler，并把这里的匿名类抽取出来作为一个单独的类。
             */
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeServerHandler());
                }
            });
            /**
             * 剩下的事情就是绑定端口并启动服务器，这里我们绑定到机器的8080端口。你可以多次调用bind()(基于不同的地址)。
             * Bind and start to accept incoming connections.(绑定并开始接受传入的连接)
             */
            ChannelFuture f = bootstrap.bind(SocketServer.port).sync();
            /**
             * Wait until the server socket is closed.(等待，直到服务器套接字关闭)
             * In this example, this does not happen, but you can do that to gracefully(在本例中，这种情况不会发生，但是您可以优雅地这样做)
             * shut down your server.(关闭你的服务)
             */
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
        new TimeServer().run();
    }

}