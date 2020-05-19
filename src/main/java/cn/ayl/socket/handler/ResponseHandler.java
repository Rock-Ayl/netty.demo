package cn.ayl.socket.handler;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.config.Const;
import cn.ayl.common.json.JsonObject;
import cn.ayl.util.TypeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;

/**
 * Created By Rock-Ayl 2019-11-14
 * 请求响应处理程序
 */
public class ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    /**
     * 响应一般http请求并返回object
     *
     * @param ctx
     * @param status
     * @param result 返回结果,一般为Json
     */
    public static void sendObject(ChannelHandlerContext ctx, HttpResponseStatus status, Object result) {
        //获取缓冲
        ByteBuf content = Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        //请求初始化
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        //判空
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
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
    public static void sendFileStream(ChannelHandlerContext ctx, File file, FileRequestType fileRequestType) throws IOException {
        //文件名
        String fileName = file.getName();
        //一个基础的OK请求
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
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
        //告诉浏览器文件类型
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, disposition);
        //todo 组装一些需要然前端知道的参数(这东西还得想象放哪)
        response.headers().add("access-control-allow-origin", "*");
        response.headers().add("access-control-allow-credentials", true);
        response.headers().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.headers().add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type,Content-Length,cookieId,fileName,fileId,type");
        response.headers().add("Access-Control-Max-Age", 86400);
        //写入响应及对应handlers
        ctx.write(response);
        //写入只读的文件流 (FileChannel放入Netty的FileRegion中)
        ctx.write(new DefaultFileRegion(new RandomAccessFile(file, "r").getChannel(), 0, file.length()));
        //ctx响应并关闭(如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成)
        ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

}
