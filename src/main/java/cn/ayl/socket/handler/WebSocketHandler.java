package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.socket.decoder.HttpDecoder;
import cn.ayl.socket.decoder.WebSocketDecoder;
import cn.ayl.socket.server.SocketServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * created by Rock-Ayl 2019-11-4
 * WebSocket请求处理服务
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    protected static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * 启动
     *
     * @throws Exception
     */
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = SocketServer.createDefaultWebSocketServerBootstrap(bossGroup, workerGroup);
            bootstrap.childHandler(new WebSocketDecoder());
            ChannelFuture f = bootstrap.bind(Const.socketPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    //读到客户端的内容并且向客户端去写内容
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //todo 服务器接收的话
        logger.info("收到消息：" + msg.text());
        /**
         * writeAndFlush接收的参数类型是Object类型，但是一般我们都是要传入管道中传输数据的类型，比如我们当前的demo
         * 传输的就是TextWebSocketFrame类型的数据
         */
        //todo 服务器响应的话
        while (true) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("服务时间：" + LocalDateTime.now()));
        }
    }

    //每个channel都有一个唯一的id值
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //打印出channel唯一值，asLongText方法是channel的id的全名
        logger.info("handlerAdded：" + ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("handlerRemoved：" + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocket Exception!");
        ctx.close();
    }

}
