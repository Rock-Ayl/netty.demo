package cn.ayl.socket.test.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 时间服务处理
 * 时间协议：国际通用的时间协议，返回可识别的时间
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 因为要在刚建立连接时发送消息而不管后来接收到的数据，这次我们不能使用channelRead(),而是用channelActive()代替
     * 当连接被建立后channelActive()方法会被调用，我们在方法体中发送一个32位的代表当前时间的整数
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        /**
         * 要发送一个新的消息，需要分配一个新的buffer(缓冲区) 去包含这个消息。
         * 我们要写一个32位的整数，因此缓冲区ByteBuf的容量至少是4个字节。
         * 通过ChannelHandlerContext.alloc()获取ByteBufAllocator(字节缓冲区分配器)，用他来分配一个新的buffer
         */
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        /**
         * 像往常一样把消息写到网络上。
         * 等一下Σ( ° △ °|||)，flip()方法哪去了？还记不记得在NIO中曾经使用过的java.nio.ByteBuffer.flip()(简单总结就是把ByteBuffer从写模式变成读模式)？ByteBuf并没有这个方法，因为他包含了两个指针——读指针和写指针 (读写标记，不要理解成C里的指针)。当你往ByteBuf写数据时，写指针会移动而读指针不变。这两个指针恰好标记着数据的起始、终止位置。
         * 与之相反，原生 NIO 并没有提供一个简洁的方式去标记数据的起始和终止位置，你必须要调用flip方法。有 时候你很可能忘记调用flip方法，导致发送不出数据或发送了错误数据。这样的错误并不会发生在 netty，因为 netty 有不同的指针去应对不同的操作 (读写操作)，这使得编程更加简单，因为你不再需要 flipping out (疯狂输出原生 NIO)
         * 其他需要注意的是ChannelHandlerContext.write()/writeAndFlush()方法返回了ChannelFuture。ChannelFuture表示一个还没发生的 I/O 操作。这意味着你请求的一些 I/O 操作可能还没被处理，因为 netty 中所有的操作都是异步的。
         * 所以，你要在 (write()返回的)ChannelFuture完成之后再调用close()。当write操作完成后，ChannelFuture会通知到他的listeners(监听器)。需加注意，close()方法可能不会立即关闭链接，同样close()也会返回一个ChannelFuture
         */
        final ChannelFuture f = ctx.writeAndFlush(time);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        });
        /**
         * 那么我们如何知道写操作完成了？
         * 很简单，只要向ChannelFuture注册监听器 (ChannelFutureListener) 就行。
         * 这一步，我们创建了ChannelFutureListener的匿名类，在写操作完成时关闭链接。
         * 你也可以使用已经定义好的监听器
         */
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //todo 记录异常
        cause.printStackTrace();
        //关闭连接
        ctx.close();
    }

}

