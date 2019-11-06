package cn.ayl.socket.decoder;

import cn.ayl.socket.handler.HttpHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-11-5
 * Http请求的解码器，一个请求需要先从这里走，实现init
 */
public class HttpDecoder extends ChannelInitializer<SocketChannel> {

    protected static Logger logger = LoggerFactory.getLogger(HttpDecoder.class);

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.info(" Http Start InitChannel .");
        // 请求解码器
        ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
        // 将HTTP消息的多个部分合成一条完整的HTTP消息
        ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65535));
        // 响应转码器
        ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
        // 解决大码流的问题，ChunkedWriteHandler：向客户端发送HTML5文件
        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        // 自定义处理handler
        ch.pipeline().addLast("http-server", new HttpHandler());
    }

}
