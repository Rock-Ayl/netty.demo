package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.util.json.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.*;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * Created By Rock-Ayl 2019-11-14
 * 请求响应处理程序
 */
public class ResponseHandler {

    /**
     * 生成Http响应OK并返回Response-Json
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    public static FullHttpResponse getResponseOKAndJson(HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }

    /**
     * 生成Http响应OK并返回Response-Text
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    public static FullHttpResponse getResponseOKAndText(HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }

    /**
     * 接受文本，直接用ctx直接发送消息
     *
     * @param ctx
     * @param status
     * @param content
     */
    public static void sendMessage(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        JsonObject result = JsonObject.Success();
        result.append("message", content);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(result.toJson(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 接受文件，直接用ctx发送流
     *
     * @param ctx
     * @throws IOException
     */
    public static void sendStream(ChannelHandlerContext ctx, HttpRequest req, File file) throws IOException {
        //一个基础的OK请求
        HttpResponse response = new DefaultHttpResponse(req.protocolVersion(), HttpResponseStatus.OK);
        //添加响应流类型
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, Const.parseHttpResponseContentType(file.getPath()));
        //handlers添加文件长度
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        //写入响应及对应handlers
        ctx.write(response);
        //写入文件流 (FileChannel放入Netty的FileRegion中)
        ctx.write(new DefaultFileRegion(new RandomAccessFile(file, "r").getChannel(), 0, file.length()));
        //ctx响应并关闭
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

}
