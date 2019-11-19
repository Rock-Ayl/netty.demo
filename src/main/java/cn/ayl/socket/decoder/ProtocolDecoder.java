package cn.ayl.socket.decoder;

import cn.ayl.config.Const;
import cn.ayl.socket.handler.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * created by Rock-Ayl 2019-11-19
 * 请求协议解码分发器
 */
public class ProtocolDecoder extends ByteToMessageDecoder {

    protected static Logger logger = LoggerFactory.getLogger(ProtocolDecoder.class);

    /**
     * 请求先从这里走，识别各类协议请求
     *
     * @param channelCtx
     * @param buffer
     * @param out
     */
    @Override
    protected void decode(ChannelHandlerContext channelCtx, ByteBuf buffer, List<Object> out) {
        if (buffer.readableBytes() < 8) {
            return;
        }
        ChannelPipeline p = channelCtx.pipeline();
        Const.RequestType requestType;
        String header = this.readHeader(buffer);
        /*过滤chrome的跨越访问*/
        if (header.startsWith("GET /favicon.ico")) {
            return;
            /*识别为WebSocket*/
        } else if (header.startsWith("<policy") || header.indexOf("Connection: Upgrade") > 0) {
            requestType = Const.RequestType.websocket;
            /*如下为Http请求,进行upload,download,service归类*/
        } else if (header.startsWith("POST " + Const.UploadPath)) {
            requestType = Const.RequestType.upload;
        } else if (header.startsWith("GET " + Const.DownloadPath)) {
            requestType = Const.RequestType.download;
        } else {
            requestType = Const.RequestType.http;
        }
        logger.info("decode header={}&contextType={}", header, requestType.name());
        //分发协议
        switchProtocol(p, requestType);
    }

    /**
     * 读取请求Header
     *
     * @param buffer
     * @return
     */
    private String readHeader(ByteBuf buffer) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int readerIndex = buffer.readerIndex();
        int count = 0;
        try {
            byte b;
            while (count <= 2 && buffer.isReadable()) {
                b = buffer.readByte();
                if (b == 13) {
                    count++;
                }
                bytes.write(b);
            }
            String header = StringUtils.toEncodedString(bytes.toByteArray(), CharsetUtil.UTF_8);
            return header;
        } finally {
            try {
                bytes.close();
            } catch (Exception e) {
                logger.error("readHeader close fail.");
            }
            buffer.readerIndex(readerIndex);
        }
    }

    /**
     * 协议分发器
     *
     * @param p
     */
    protected void switchProtocol(ChannelPipeline p, Const.RequestType requestType) {
        /*remove all handle for keep-alive*/
        switch (requestType) {
            //WebSocket协议
            case websocket:
                switchWebSocket(p);
                break;
            //http协议及默认
            case http:
            default:
                this.switchHttp(p, requestType);
                break;
        }
        p.remove(this);
    }

    /**
     * 分发WebSocket
     *
     * @param p
     */
    protected void switchWebSocket(ChannelPipeline p) {
        //http基础套件(WebSocket底层是Http)
        p.addLast("http-request-decoder", new HttpRequestDecoder());
        p.addLast("http-chunk-aggregator", new HttpObjectAggregator(Const.MaxContentLength));
        p.addLast("http-response-encoder", new HttpResponseEncoder());
        //心跳(防止资源浪费)
        p.addLast(new IdleStateHandler(Const.ReaderIdleTimeSeconds, Const.WriterIdleTimeSeconds, Const.AllIdleTimeSeconds));
        p.addLast("webSocket-heartBeat", new HeartBeatHandler());
        //升级为WebSocket
        p.addLast("http-WebSocketServer-protocolHandler", new WebSocketServerProtocolHandler(Const.WebSocketPath));
        //webSocket处理器
        p.addLast("webSocket-handler", new WebSocketHandler());
    }

    /**
     * 分发http请求处理器 eg: upload download service
     *
     * @param p
     */
    protected void switchHttp(ChannelPipeline p, Const.RequestType requestType) {
        //http基本解码
        p.addLast("http-request-decoder", new HttpRequestDecoder());
        //如果请求类型为上传，整合文件
        if (requestType != Const.RequestType.upload) {
            //netty是基于分段请求的，HttpObjectAggregator的作用是将HTTP消息的多个部分合成一条完整的HTTP消息,参数是聚合字节的最大长度
            p.addLast("http-chunk-aggregator", new HttpObjectAggregator(Const.MaxContentLength));
        }
        //响应编码
        p.addLast("http-response-encoder", new HttpResponseEncoder());
        //是否为下载请求
        if (requestType == Const.RequestType.download) {
            //以块的方式来写的处理器，解决大码流的问题，ChunkedWriteHandler：可以向客户端发送HTML5文件
            p.addLast("http-chunk-write", new ChunkedWriteHandler());
            //下载处理器
            p.addLast("http-download-handler", new DownloadFileHandler());
        } else {
            //默认为Http处理器
            p.addLast("http-handler", new HttpHandler());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("ProtocolDecoder Exception Channel=[{}]&Error=[{}]", ctx.channel().toString(), cause.getLocalizedMessage());
    }

}
