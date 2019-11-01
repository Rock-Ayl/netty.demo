package cn.ayl.socket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Discard Server Handler 即 抛弃 服务 处理器，用来处理IO事件,忽略所有接收到的数据
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 这里我们重写了channelRead(),当有新数据到达时该方法就会被调用，并附带接收到的数据作为方法参数
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            //解析字节缓冲并打印
            System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII));
        } finally {
            //默默的丢弃数据,调用release()直接释放资源
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 当netty发生IO错误,或者handler在处理事件抛出异常时，exceptionCaught()就会被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //todo 记录异常
        cause.printStackTrace();
        //关闭连接
        ctx.close();
    }

}
