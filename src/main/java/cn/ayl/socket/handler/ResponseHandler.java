package cn.ayl.socket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * Created By Rock-Ayl 2019-11-14
 * 请求响应处理程序
 */
public class ResponseHandler {

    /**
     * Http响应OK并返回Json
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    public static FullHttpResponse responseOKAndJson(HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }

    /**
     * Http响应OK并返回文本
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    public static FullHttpResponse responseOKAndText(HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }
}
