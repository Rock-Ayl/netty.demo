package cn.ayl.socket.decoder;

import cn.ayl.socket.handler.HttpAndWebSocketHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-11-6
 * WebSocket请求的解码器，一个请求需要先从这里走，实现init
 */
public class HttpAndWebSocketDecoder extends ChannelInitializer<SocketChannel> {

    protected static Logger logger = LoggerFactory.getLogger(HttpAndWebSocketDecoder.class);

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.info("解析请求.");
        ChannelPipeline pipeline = ch.pipeline();
        //http解码器(webSocket是http的升级)
        pipeline.addLast(new HttpServerCodec());
        //以块的方式来写的处理器，解决大码流的问题，ChunkedWriteHandler：可以向客户端发送HTML5文件
        pipeline.addLast(new ChunkedWriteHandler());
        //netty是基于分段请求的，HttpObjectAggregator的作用是将HTTP消息的多个部分合成一条完整的HTTP消息,参数是聚合字节的最大长度
        pipeline.addLast(new HttpObjectAggregator(65535));
        /**
         * WebSocketServerProtocolHandler负责websocket握手以及处理控制框架（Close，Ping（心跳检检测request），Pong（心跳检测响应））。
         * 参数为ws请求的访问路径 eg:ws://127.0.0.1:8888/WebSocket。
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/WebSocket"));
        //处理器
        pipeline.addLast(new HttpAndWebSocketHandler());
    }

}