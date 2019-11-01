package cn.ayl.socket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Echo Server Handler 即 echo 服务 处理器，用来处理IO事件
 * ECHO协议：指的是把接收到的信息按照原样返回；作用：主要用于检测和调试；这个协议可以基于TCP/UDP协议用于服务器检测端口7有无信息。
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /*这里我们重写了channelRead(),当有新数据到达时该方法就会被调用，并附带接收到的数据作为方法参数*/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        /**
         * ChannelHandlerContext能触发多种 I/O 事件和操作，这里我们调用write()方法逐字写回接收到的数据。
         * 请注意我们并没有释放接收到的消息Object msg，因为在写数据时ctx.write(msg)，netty 已经帮你释放它了。
         */
        ctx.write(msg);
        /**
         * ctx.write()关没有把消息写到网络上，他在内部被缓存起来，你需要调用ctx.flush()把他刷新到网络上。
         * ctx.writeAndFlush(msg)是个更简洁的方法。
         */
        ctx.flush();
    }

    /*当netty发生IO错误,或者handler在处理事件抛出异常时，exceptionCaught()就会被调用*/
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //todo 记录异常
        cause.printStackTrace();
        //关闭连接
        ctx.close();
    }

}

