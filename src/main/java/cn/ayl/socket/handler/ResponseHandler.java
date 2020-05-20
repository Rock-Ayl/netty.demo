package cn.ayl.socket.handler;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.config.Const;
import cn.ayl.common.json.JsonObject;
import cn.ayl.util.DateUtils;
import cn.ayl.util.HttpUtils;
import cn.ayl.util.TypeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created By Rock-Ayl 2019-11-14
 * 请求响应处理程序
 */
public class ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private static Set<AsciiString> Headers = new HashSet<>();

    static {
        Headers.add(AsciiString.cached("Origin"));
        Headers.add(AsciiString.cached("X-Requested-With"));
        Headers.add(AsciiString.cached("Accept"));
        //通用参数
        Headers.add(AsciiString.cached("params"));
        //cookieId
        Headers.add(AsciiString.cached("cookieId"));
        Headers.add(HttpHeaderNames.CONTENT_TYPE);
        Headers.add(HttpHeaderNames.CONTENT_LENGTH);
        Headers.add(HttpHeaderNames.AUTHORIZATION);
    }

    /**
     * 设置通用响应headers
     *
     * @param response
     */
    private static void setServerHeaders(HttpResponse response) {
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        headers.set(HttpHeaderNames.SERVER, "netty.demo");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, Collections.unmodifiableSet(Headers));
        headers.set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, 86400);
    }

    /**
     * 响应一般http请求并返回object
     *
     * @param ctx
     * @param status
     * @param result 返回结果,一般为Json
     */
    public static void sendObject(ChannelHandlerContext ctx, HttpResponseStatus status, Object result) {
        //创建一个新缓冲
        ByteBuf content = Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        //请求初始化
        FullHttpResponse response = new DefaultFullHttpResponse(Const.CurrentHttpVersion, status, content);
        //判空
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        //添加通用参数
        setServerHeaders(response);
        //响应并关闭通道
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 响应http并返回Json,包含Message
     *
     * @param ctx
     * @param status
     * @param content
     */
    public static void sendMessageOfJson(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        sendObject(ctx, status, JsonObject.Success().append(Const.Message, content));
    }

    /**
     * 响应预检请求
     *
     * @param ctx
     */
    public static void sendOption(ChannelHandlerContext ctx) {
        sendObject(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "ok");
    }

    /**
     * 响应并返回请求下载文件的文件流
     *
     * @param ctx
     * @throws IOException
     */
    public static void sendFileStream(ChannelHandlerContext ctx, HttpRequest request, File file, FileRequestType fileRequestType) throws IOException {
        //文件名
        String fileName = file.getName();
        //当前时间
        long thisTime = System.currentTimeMillis();
        //一个基础的OK请求
        HttpResponse response = new DefaultHttpResponse(Const.CurrentHttpVersion, HttpResponseStatus.OK);
        //handlers添加文件长度
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        //初始化文件类型
        String contentType;
        //设定化内容处理:以附件的形式下载、文件名、编码
        String disposition;
        //根据文件请求类型判定
        switch (fileRequestType) {
            //只是下载
            case download:
                //所有下载都是流
                contentType = "application/octet-stream; charset=utf-8";
                //告诉浏览器是下载,文件名
                disposition = "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "utf-8");
                break;
            //只是预览
            case preview:
            default:
                //按文件类别区分文件类型
                contentType = TypeUtils.parseHttpResponseContentType(fileName);
                //告诉浏览器是预览,文件名
                disposition = "inline; filename*=UTF-8''" + URLEncoder.encode(fileName, "utf-8");
                break;
        }
        //文件内容类型
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        //文件名,是否 save as
        response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, disposition);
        //该资源发送的时间
        response.headers().set(HttpHeaderNames.DATE, DateUtils.SDF_HTTP_DATE_FORMATTER.format(thisTime));
        //响应过期的日期和时间
        response.headers().set(HttpHeaderNames.EXPIRES, DateUtils.SDF_HTTP_DATE_FORMATTER.format(thisTime + Const.FileResourceExpiresTime));
        //设置缓存开关
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + Const.FileResourceExpiresTime);
        //文件最后修改时间
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, DateUtils.SDF_HTTP_DATE_FORMATTER.format(new Date(file.lastModified())));
        //添加通用参数
        setServerHeaders(response);
        //写入响应及对应handlers
        ctx.write(response);
        //对于http和https协议使用不同的传输文件方式
        if (HttpUtils.isHttps(ctx)) {
            //https的传输文件方式
            ctx.write(new HttpChunkedInput(new ChunkedFile(new RandomAccessFile(file, "r"), 0, file.length(), Const.ChunkSize)), ctx.newProgressivePromise());
        } else {
            //http的传输文件方式,零拷贝,高效
            ctx.write(new DefaultFileRegion(new RandomAccessFile(file, "r").getChannel(), 0, file.length()), ctx.newProgressivePromise());
        }
        //ctx响应并关闭(如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成)
        ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

}
