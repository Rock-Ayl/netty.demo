package cn.ayl.socket.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-11-17
 * WebSocket心跳处理程序
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object msg) throws Exception {
        //如果通道触发了闲置检测
        if (msg instanceof IdleStateEvent) {
            //获取闲置状态
            IdleStateEvent event = (IdleStateEvent) msg;
            //根据状态操作
            switch (event.state()) {
                //读空闲
                case READER_IDLE:
                    logger.info("进入读空闲...");
                    break;
                //写空闲
                case WRITER_IDLE:
                    logger.info("进入写空闲...");
                    break;
                //读写空闲
                case ALL_IDLE:
                    logger.info("开始杀死无用通道，节约资源");
                    Channel channel = ctx.channel();
                    channel.close();
                    break;
            }
        }

    }

}