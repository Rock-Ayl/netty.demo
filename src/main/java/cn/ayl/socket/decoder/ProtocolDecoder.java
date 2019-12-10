package cn.ayl.socket.decoder;

import cn.ayl.config.Const;
import cn.ayl.rpc.Context;
import cn.ayl.socket.handler.DownloadFileHandler;
import cn.ayl.socket.handler.HeartBeatHandler;
import cn.ayl.socket.handler.HttpHandler;
import cn.ayl.socket.handler.WebSocketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
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
 * 协议解码器
 */
public class ProtocolDecoder extends ChannelInitializer<SocketChannel> {

    protected static Logger logger = LoggerFactory.getLogger(ProtocolDecoder.class);

    //每一个通道都有一个上下文，上下文给与通道
    private Context context;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //组装实现类
        ch.pipeline().addLast(new ProtocolDecoderExecute());
    }

    /**
     * 协议解码实现类
     */
    public class ProtocolDecoderExecute extends ByteToMessageDecoder {

        private HttpHandler httpHandler;
        private DownloadFileHandler downloadFileHandler;
        private WebSocketHandler webSocketHandler;

        public ProtocolDecoderExecute() {
            this.httpHandler = new HttpHandler();
            this.downloadFileHandler = new DownloadFileHandler();
            this.webSocketHandler = new WebSocketHandler();
        }

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
            Channel channel = p.channel();
            //断点续传检查一下
            context = channel.attr(Const.AttrContext).get();
            //如果上下文存在空，直接返回
            if (context != null) {
                return;
            }
            String header = this.readHeader(buffer);
            /*过滤chrome的跨越访问*/
            if (header.startsWith("GET /favicon.ico")) {
                return;
                /*识别为WebSocket并绑定上下文*/
            } else if (header.startsWith("<policy") || header.indexOf("Connection: Upgrade") > 0) {
                context = Context.createContext(Const.RequestType.websocket, channel);
                /*如下为Http请求,进行upload,download,service归类并绑定上下文*/
            } else if (header.startsWith("POST " + Const.UploadPath)) {
                context = Context.createContext(Const.RequestType.upload, channel);
            } else if (header.startsWith("GET " + Const.DownloadPath)) {
                context = Context.createContext(Const.RequestType.download, channel);
            } else {
                context = Context.createContext(Const.RequestType.http, channel);
            }
            logger.info("decode header={}&contextType={}", header, context.requestType.name());
            //分发协议
            switchProtocol(p);
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
        protected void switchProtocol(ChannelPipeline p) {
            //根据请求类型分发
            switch (context.requestType) {
                //WebSocket协议
                case websocket:
                    switchWebSocket(p);
                    break;
                //http协议及默认
                case http:
                default:
                    this.switchHttp(p, context.requestType);
                    break;
            }
            p.remove(this);
            //通道绑定上下文
            p.channel().attr(Const.AttrContext).set(context);
        }

        /**
         * 分发WebSocket请求解析器
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
            p.addLast("webSocket-handler", webSocketHandler);
        }

        /**
         * 分发http请求解析器 eg: upload download service
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
                p.addLast("http-download-handler", downloadFileHandler);
            } else {
                //默认为Http处理器
                p.addLast("http-handler", httpHandler);
            }
        }

        /**
         * 异常
         *
         * @param ctx
         * @param cause
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("ProtocolDecoderExecute Exception Channel=[{}]&Error=[{}]", ctx.channel().toString(), cause.getLocalizedMessage());
        }

    }
}
