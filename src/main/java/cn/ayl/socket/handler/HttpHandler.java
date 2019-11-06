package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.json.JsonObject;
import cn.ayl.socket.decoder.HttpDecoder;
import cn.ayl.socket.server.SocketServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import cn.ayl.json.JsonUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * created by Rock-Ayl 2019-11-4
 * Http请求处理服务
 */
public class HttpHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    /**
     * 请求解码后，从通道读取,进行分类处理
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof HttpContent) {
                //todo handleHttpContent
            }
        } catch (Exception e) {
            logger.error("channelRead", e);
        } finally {
            ReferenceCountUtil.safeRelease(msg);
        }
    }

    /**
     * 启动
     *
     * @throws Exception
     */
    public void start() throws Exception {
        /**
         * NioEventLoopGroup是一个处理I/O操作的事件循环器 (其实是个线程池)。
         * netty为不同类型的传输协议提供了多种NioEventLoopGroup的实现。
         * 在本例中我们要实现一个服务端应用，并使用了两个NioEventLoopGroup。
         * 第一个通常被称为boss，负责接收已到达的 connection。
         * 第二个被称作 worker，当 boss 接收到 connection 并把它注册到 worker 后，worker 就可以处理 connection 上的数据通信。
         * 要创建多少个线程，这些线程如何匹配到Channel上会随着EventLoopGroup实现的不同而改变，或者你可以通过构造器去配置他们。
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /**
             * ServerBootstrap是用来搭建 server 的协助类。
             * 你也可以直接使用Channel搭建 server，然而这样做步骤冗长，不是一个好的实践，大多数情况下建议使用ServerBootstrap。
             */
            ServerBootstrap bootstrap = SocketServer.createDefaultHttpServerBootstrap(bossGroup, workerGroup);
            /**
             * 这里的 handler 会被用来处理新接收的Channel。
             * ChannelInitializer是一个特殊的 handler，
             * 帮助开发者配置Channel，而多数情况下你会配置Channel下的ChannelPipeline，
             * 往 pipeline 添加一些 handler (例如DiscardServerHandler) 从而实现你的应用逻辑。
             * 当你的应用变得复杂，你可能会向 pipeline 添加更多的 handler，并把这里的匿名类抽取出来作为一个单独的类。
             */
            bootstrap.childHandler(new HttpDecoder());
            /**
             * 剩下的事情就是绑定端口并启动服务器，这里我们绑定到机器的8080端口。你可以多次调用bind()(基于不同的地址)。
             * Bind and start to accept incoming connections.(绑定并开始接受传入的连接)
             */
            ChannelFuture f = bootstrap.bind(Const.socketPort).sync();
            /**
             * Wait until the server socket is closed.(等待，直到服务器套接字关闭)
             * In this example, this does not happen, but you can do that to gracefully(在本例中，这种情况不会发生，但是您可以优雅地这样做)
             * shut down your server.(关闭你的服务)
             */
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 处理业务
     *
     * @param path   eg:  /Organize/login
     * @param params eg:  user:root pwd:123456
     * @return
     */
    private JsonObject handleServiceFactory(String path, Map<String, Object> params) {
        //todo 根据path和params处理业务并返回
        return JsonObject.Success();
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //todo http请求内容分类,目前设定为全部为服务请求(可以存在页面,资源,上传等等)
        handleService(ctx, req);
    }

    //处理http服务请求
    private void handleService(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse response;
        JsonObject result;
        //获得请求path
        String path = getPath(req);
        //根据请求类型处理请求 get post ...
        if (req.method() == HttpMethod.GET) {
            //获取请求参数
            Map<String, Object> params = getGetParamsFromChannel(req);
            //业务
            result = handleServiceFactory(path, params);
            response = responseOKAndJson(HttpResponseStatus.OK, result);
        } else if (req.method() == HttpMethod.POST) {
            //获取请求参数
            Map<String, Object> params = getPostParamsFromChannel(req);
            //处理业务
            result = handleServiceFactory(path, params);
            response = responseOKAndJson(HttpResponseStatus.OK, result);
        } else {
            //todo 处理其他类型的请求
            response = responseOKAndJson(HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
        }
        // 发送响应并关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 获取请求Path
     *
     * @param req
     * @return
     */
    private String getPath(FullHttpRequest req) {
        String path = null;
        try {
            path = new URI(req.getUri()).getPath();
        } catch (Exception e) {
            logger.error("接口解析错误.");
        } finally {
            return path;
        }
    }

    /**
     * 获取GET方式传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getGetParamsFromChannel(FullHttpRequest fullHttpRequest) {
        //参数组
        Map<String, Object> params = new HashMap<>();
        //如果请求为GET继续
        if (fullHttpRequest.method() == HttpMethod.GET) {
            // 处理get请求
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        } else {
            return null;
        }

    }

    /**
     * 获取POST方式传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {
        //参数组
        Map<String, Object> params;
        //如果请求为POST
        if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get("Content-Type").trim();
            if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(fullHttpRequest);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJSONParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            } else {
                return null;
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<>();
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        return params;
    }

    /**
     * 解析json数据（Content-Type = application/json）
     *
     * @param fullHttpRequest
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, Object> getJSONParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<>();
        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");
        JsonObject jsonParams = JsonUtil.parse(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get((String) key));
        }
        return params;
    }

    /**
     * 响应OK并返回Json
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    private FullHttpResponse responseOKAndJson(HttpResponseStatus status, JsonObject result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }

    /**
     * 响应OK并返回文本
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    private FullHttpResponse responseOKAndText(HttpResponseStatus status, String result) {
        ByteBuf content = copiedBuffer(result, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }

}