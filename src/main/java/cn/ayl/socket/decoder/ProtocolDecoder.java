package cn.ayl.socket.decoder;

import cn.ayl.common.enumeration.RequestType;
import cn.ayl.config.Const;
import cn.ayl.socket.rpc.Context;
import cn.ayl.socket.handler.*;
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
import java.util.concurrent.TimeUnit;

/**
 * 协议解码器
 */
public class ProtocolDecoder extends ChannelInitializer<SocketChannel> {

    protected static Logger logger = LoggerFactory.getLogger(ProtocolDecoder.class);

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //组装实现类
        ch.pipeline().addLast(new ProtocolDecoderExecute());
    }

    /**
     * 协议解码实现类
     */
    public class ProtocolDecoderExecute extends ByteToMessageDecoder {

        //每一个通道都有一个上下文，上下文给与通道
        private Context context;

        /**
         * 请求先从这里走，识别各类协议请求
         *
         * @param channelCtx
         * @param buffer
         * @param out
         */
        @Override
        protected void decode(ChannelHandlerContext channelCtx, ByteBuf buffer, List<Object> out) {
            //过滤请求大小
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
            //区分下网络协议并创建上下文
            distinguishNetworkProtocol(buffer, channel);
            //根据网络协议分发解析器
            switchProtocol(p);
            p.remove(this);
            //通道绑定上下文,以后用get获取
            p.channel().attr(Const.AttrContext).set(context);
        }

        /**
         * 区分网络协议，目前仅仅识别http和websocket
         */
        private void distinguishNetworkProtocol(ByteBuf buffer, Channel channel) {
            String header = this.readHeader(buffer);
            //判断是否为webSocket
            if (isWebSocket(header)) {
                context = Context.createInitContext(RequestType.websocket, channel);
                /*如下为Http请求,进行upload,download,service归类并绑定上下文*/
            } else if (header.startsWith("POST " + Const.UploadPath)) {
                context = Context.createInitContext(RequestType.upload, channel);
            } else if (header.startsWith("GET " + Const.DownloadPath)) {
                context = Context.createInitContext(RequestType.download, channel);
            } else {
                context = Context.createInitContext(RequestType.http, channel);
            }
            logger.info("decode header={}&contextType={}", header, context.requestType.name());
        }

        /**
         * 读取请求Header
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
         * 判断一个请求是否为WebSocket
         *
         * @param header 基于 netty ByteBuf 读出的header
         * @return
         */
        private boolean isWebSocket(String header) {
            if (header.startsWith("<policy") || header.indexOf("Connection: Upgrade") > 0) {
                return true;
            }
            return false;
        }

        /**
         * 简单判断请求并分发协议、绑定上下文
         */
        private void switchProtocol(ChannelPipeline p) {
            //根据请求类型分发
            switch (context.requestType) {
                //WebSocket协议
                case websocket:
                    switchWebSocket(p);
                    break;
                //http协议及默认
                case http:
                default:
                    this.switchHttp(p);
                    break;
            }
        }

        /**
         * 分发WebSocket请求解析器
         *
         * @param p
         */
        private void switchWebSocket(ChannelPipeline p) {
            //基础套件
            httpAddLast(p);
            //聚合套件
            aggregatorAddLast(p);
            //心跳套件
            heartAddLast(p);
            //升级WebSocket套件及处理器
            webSocketAddLast(p);
        }

        /**
         * 分发http请求解析器 eg: upload download service
         *
         * @param p
         */
        private void switchHttp(ChannelPipeline p) {
            //基础套件
            httpAddLast(p);
            //判断是否不为上传请求
            if (context.requestType != RequestType.upload) {
                //聚合套件
                aggregatorAddLast(p);
            }
            //判断是否为下载请求
            if (context.requestType == RequestType.download) {
                //下载套件及处理器
                downloadAddLast(p);
            } else {
                //默认都用http处理器
                httpHandlerAddLast(p);
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

    //所有http必须要用到的套件
    public static void httpAddLast(ChannelPipeline p) {
        //http基本解码
        p.addLast("http-request-decoder", new HttpRequestDecoder());
        //过滤器
        p.addLast("http-auth", new FilterHandler());
        //响应编码
        p.addLast("http-response-encoder", new HttpResponseEncoder());
    }

    //聚合套件
    public static void aggregatorAddLast(ChannelPipeline p) {
        //netty是基于分段请求的，HttpObjectAggregator的作用是将HTTP消息的多个部分合成一条完整的HTTP消息,参数是聚合字节的最大长度
        p.addLast("http-chunk-aggregator", new HttpObjectAggregator(Const.MaxContentLength));
    }

    //心跳套件及处理器
    public static void heartAddLast(ChannelPipeline p) {
        //初始化心跳设置 读、写、读写 以及 心跳单位
        p.addLast(new IdleStateHandler(Const.ReaderIdleTimeSeconds, Const.WriterIdleTimeSeconds, Const.AllIdleTimeSeconds, TimeUnit.SECONDS));
        //心跳处理器
        p.addLast("webSocket-heartBeat", new HeartBeatHandler());
    }

    //升级WebSocket套件及处理器
    public static void webSocketAddLast(ChannelPipeline p) {
        //升级为WebSocket
        p.addLast("http-WebSocketServer-protocolHandler", new WebSocketServerProtocolHandler(Const.WebSocketPath));
        //webSocket处理器
        p.addLast("webSocket-handler", new WebSocketHandler());
    }

    //下载套件及处理器
    public static void downloadAddLast(ChannelPipeline p) {
        //以块的方式来写的处理器，解决大码流的问题，ChunkedWriteHandler：可以向客户端发送HTML5文件
        if (p.get("http-download-handler") == null) {
            p.addLast("http-chunk-write", new ChunkedWriteHandler());
            //下载处理器
            p.addLast("http-download-handler", new DownloadFileHandler());
        }
    }

    //http处理器
    public static void httpHandlerAddLast(ChannelPipeline p) {
        if (p.get("http-handler") == null) {
            p.addLast("http-handler", new HttpHandler());
        }
    }

    //清除下载套件及处理器
    public static void clearDownloadAddLast(ChannelPipeline p) {
        clearPipe(p, "http-chunk-write");
        clearPipe(p, "http-download-handler");
    }

    //清除http处理器
    public static void clearHttpHandlerAddLast(ChannelPipeline p) {
        clearPipe(p, "http-handler");
    }

    //清除解码器
    private static void clearPipe(ChannelPipeline p, String name) {
        if (p.get(name) != null) {
            p.remove(name);
        }
    }
}
