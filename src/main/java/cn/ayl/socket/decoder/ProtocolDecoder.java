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

    //http套件
    private static final String HttpRequestDecoderName = "http-request-decoder";
    private static final String HttpAuthFilterName = "http-auth-Filter";
    private static final String HttpResponseEncoderName = "http-response-encoder";
    private static final String httpChunkAggregatorName = "http-chunk-aggregator";
    private static final String HttpChunkWriteName = "http-chunk-write";

    //webSocket套件
    private static final String WebSocketIdleStateName = "webSocket-idleState";
    private static final String WebSocketHeartBeatName = "webSocket-heartBeat";
    private static final String WebSocketServerProtocolHandlerName = "webSocket-Server-protocolHandler";

    //业务处理器
    private static final String HttpHandlerName = "http-handler";
    private static final String HttpDownloadHandlerName = "http-download-handler";
    private static final String webSocketHandlerName = "webSocket-handler";

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
            //获取通道处理器管道
            ChannelPipeline p = channelCtx.pipeline();
            //获取通道
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
            //将所有所需的ChannelHandler添加到pipeline之后,一定要将自身移除掉,否则该Channel之后的请求仍会重新执行协议的分发，而这是要避免的
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
            logger.info("decode header={} ,&contextType={}", header, context.requestType.name());
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
                //获取header并返回
                return StringUtils.toEncodedString(bytes.toByteArray(), CharsetUtil.UTF_8);
            } finally {
                try {
                    //关闭
                    bytes.close();
                } catch (Exception e) {
                    logger.error("readHeader close fail:", e);
                    return null;
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
            //输入日志
            logger.error("ProtocolDecoderExecute Exception Channel=[{}]&Error=[{}]", ctx.channel().toString(), cause.getLocalizedMessage());
            //关闭
            ctx.close();
        }
    }

    //所有http必须要用到的套件
    public static void httpAddLast(ChannelPipeline p) {
        //http请求解码
        p.addLast(HttpRequestDecoderName, new HttpRequestDecoder());
        //身份验证/过滤器
        p.addLast(HttpAuthFilterName, new FilterHandler());
        //http响应编码
        p.addLast(HttpResponseEncoderName, new HttpResponseEncoder());
    }

    //聚合套件
    public static void aggregatorAddLast(ChannelPipeline p) {
        //netty是基于分段请求的，HttpObjectAggregator的作用是将HTTP消息的多个部分合成一条完整的HTTP消息,参数是聚合字节的最大长度
        p.addLast(httpChunkAggregatorName, new HttpObjectAggregator(Const.MaxContentLength));
    }

    //心跳套件及处理器
    public static void heartAddLast(ChannelPipeline p) {
        //初始化心跳设置 读、写、读写 以及 心跳单位
        p.addLast(WebSocketIdleStateName, new IdleStateHandler(Const.ReaderIdleTimeSeconds, Const.WriterIdleTimeSeconds, Const.AllIdleTimeSeconds, TimeUnit.SECONDS));
        //心跳处理器
        p.addLast(WebSocketHeartBeatName, new HeartBeatHandler());
    }

    //升级WebSocket套件及处理器
    public static void webSocketAddLast(ChannelPipeline p) {
        //升级为WebSocket
        p.addLast(WebSocketServerProtocolHandlerName, new WebSocketServerProtocolHandler(Const.WebSocketPath));
        //webSocket处理器
        p.addLast(webSocketHandlerName, new WebSocketHandler());
    }

    //下载套件及处理器
    public static void downloadAddLast(ChannelPipeline p) {
        //判空
        if (p.get(HttpDownloadHandlerName) == null) {
            //以块的方式来写的处理器，解决大码流的问题，ChunkedWriteHandler：可以向客户端发送HTML5文件
            p.addLast(HttpChunkWriteName, new ChunkedWriteHandler());
            //下载处理器
            p.addLast(HttpDownloadHandlerName, new DownloadFileHandler());
        }
    }

    //http处理器
    public static void httpHandlerAddLast(ChannelPipeline p) {
        if (p.get(HttpHandlerName) == null) {
            p.addLast(HttpHandlerName, new HttpHandler());
        }
    }

    //清除下载套件及处理器
    public static void clearDownloadAddLast(ChannelPipeline p) {
        clearPipe(p, HttpChunkWriteName);
        clearPipe(p, HttpDownloadHandlerName);
    }

    //清除http处理器
    public static void clearHttpHandlerAddLast(ChannelPipeline p) {
        clearPipe(p, HttpHandlerName);
    }

    //清除解码器
    private static void clearPipe(ChannelPipeline p, String name) {
        //判空
        if (p.get(name) != null) {
            //清除
            p.remove(name);
        }
    }
}
