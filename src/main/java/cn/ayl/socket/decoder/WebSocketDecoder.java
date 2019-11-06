package cn.ayl.socket.decoder;

import cn.ayl.socket.handler.WebSocketHandler;
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
public class WebSocketDecoder extends ChannelInitializer<SocketChannel> {

    protected static Logger logger = LoggerFactory.getLogger(WebSocketDecoder.class);

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.info(" WebSocket Start InitChannel .");
        ChannelPipeline pipeline = ch.pipeline();
        //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
        pipeline.addLast(new HttpServerCodec());
        //以块的方式来写的处理器
        pipeline.addLast(new ChunkedWriteHandler());
        //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
        pipeline.addLast(new HttpObjectAggregator(8192));
        //ws://server:port/context_path
        //ws://localhost:9999/ws
        /**
         * WebSocketServerProtocolHandler：参数是访问路径，这边指定的是ws，服务客户端访问服务器的时候指定的url是：ws://127.0.0.1:8888/ws。
         * 它负责websocket握手以及处理控制框架（Close，Ping（心跳检检测request），Pong（心跳检测响应））。
         * 文本和二进制数据帧被传递到管道中的下一个处理程序进行处理。
         */
        //参数指的是context_path
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //websocket定义了传递数据的6中frame类型
        pipeline.addLast(new WebSocketHandler());
    }

}