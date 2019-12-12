package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.rpc.Context;
import cn.ayl.util.StringUtil;
import cn.ayl.util.json.JsonObject;
import cn.ayl.util.json.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl on 2019-11-18
 * todo WebSocket处理器
 */
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    //上下文
    private Context context;

    //用来关闭WebSocket
    private WebSocketServerHandshaker webSocketServerHandshaker;

    /**
     * 通道，webSocket读取请求从这里过来
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取上下文
        context = ctx.channel().attr(Const.AttrContext).get();
        //无上下文，返回
        if (context == null) {
            return;
        }
        //处理WebSocket请求的分别处理
        if (msg instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 每个channel都有一个唯一的id值
     * asLongText方法是channel的id的全名
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //todo 连接打开时
        logger.info(ctx.channel().localAddress().toString() + " ,handlerAdded！, channelId=" + ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("通道不活跃的");
        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("正在执行channelActive()方法.....");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //todo 连接关闭时
        logger.info(ctx.channel().localAddress().toString() + " ,handlerRemoved！, channelId=" + ctx.channel().id().asLongText());
    }

    // 处理Websocket的代码
    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 文本消息，不支持二进制消息
        if (frame instanceof TextWebSocketFrame) {
            //请求text
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("收到信息:" + request);
            //todo 写Json和text处理器
            if (StringUtil.isJson(request)) {
                //返回
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.parse(request).success().toString()));
            } else {
                //返回
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonObject.Success().append("req", request).toString()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //todo 出现异常
        logger.error("Client:" + ctx.channel().remoteAddress() + " ,error", cause.getMessage());
        ctx.close();
    }

}
