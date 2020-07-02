package cn.ayl.socket.inboundHandler;

import cn.ayl.config.Const;
import cn.ayl.socket.rpc.Context;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl on 2019-11-18
 * WebSocket处理器
 */
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    //上下文
    private Context context;

    //用来关闭WebSocket
    protected WebSocketServerHandshaker webSocketServerHandshaker;

    //一个群聊所有的人
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 通道，webSocket读取请求从这里过来
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取上下文
        this.context = ctx.channel().attr(Const.AttrContext).get();
        //无上下文，返回
        if (this.context == null) {
            return;
        }
        //如果是webSocket
        if (msg instanceof WebSocketFrame) {
            //处理WebSocket请求
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 处理Websocket的代码
     *
     * @param ctx
     * @param frame
     */
    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            //关闭
            this.webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        //判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //文本消息,不支持二进制消息
        if (frame instanceof TextWebSocketFrame) {
            //获取请求文本
            String request = ((TextWebSocketFrame) frame).text();
            //log
            logger.info("收到信息:" + request);
            //当前通道
            Channel incoming = ctx.channel();
            //循环所有通道
            for (Channel channel : this.channels) {
                //如果是自己
                if (channel == incoming) {
                    //给自己发送消息
                    channel.writeAndFlush(new TextWebSocketFrame("[you]:" + request));
                } else {
                    //给自己发送消息
                    channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "]:" + request));
                }
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //获取通道
        Channel incoming = ctx.channel();
        //群发消息
        this.channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入房间"));
        //从组新增该通道
        this.channels.add(ctx.channel());
        //log
        logger.info("Client:" + incoming.remoteAddress() + "加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //获取通道
        Channel incoming = ctx.channel();
        //群发消息
        this.channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开房间"));
        //从组移除该通道(该逻辑可以删除,因为netty会自动调用)
        this.channels.remove(ctx.channel());
        //log
        logger.info("Client:" + incoming.remoteAddress() + "离开");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //获取通道
        Channel incoming = ctx.channel();
        //log
        logger.info("Client:" + incoming.remoteAddress() + "上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //获取通道
        Channel incoming = ctx.channel();
        //log
        logger.info("Client:" + incoming.remoteAddress() + "下线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //获取通道
        Channel incoming = ctx.channel();
        // 当出现异常就关闭连接
        ctx.close();
        //log
        logger.error("Client:" + incoming.remoteAddress() + "异常:[{}]", cause);
    }

}
