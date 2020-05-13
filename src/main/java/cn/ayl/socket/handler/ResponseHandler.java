package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.common.json.JsonObject;
import cn.ayl.util.TypeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

/**
 * Created By Rock-Ayl 2019-11-14
 * 请求响应处理程序
 */
public class ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    /**
     * 响应预检请求
     *
     * @param ctx
     */
    public static void sendOption(ChannelHandlerContext ctx) {
        sendForText(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "ok");
    }

    /**
     * 响应http并返回text
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    public static void sendForText(ChannelHandlerContext ctx, HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 响应http并返回json
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    public static void sendForJson(ChannelHandlerContext ctx, HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 响应http并返回Json,包含Message
     *
     * @param ctx
     * @param status
     * @param content
     */
    public static void sendMessageForJson(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        JsonObject result = JsonObject.Success().append(Const.Message, content);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(result.toJson(), CharsetUtil.UTF_8));
        //判空
        if (StringUtils.isNotEmpty(content)) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 响应并返回请求静态资源的文件流
     *
     * @param ctx
     * @throws IOException
     */
    public static void sendForResourceStream(ChannelHandlerContext ctx, File file) throws IOException {
        //一个基础的OK请求
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        //添加响应流类型
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, TypeUtils.parseHttpResponseContentType(file.getPath()));
        //handlers添加文件长度
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        //写入响应及对应handlers
        ctx.write(response);
        //写入只读的文件流 (FileChannel放入Netty的FileRegion中)
        ctx.write(new DefaultFileRegion(new RandomAccessFile(file, "r").getChannel(), 0, file.length()));
        //ctx响应并关闭(如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成)
        ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    /**
     * 响应并返回请求下载文件的文件流
     *
     * @param ctx
     * @throws IOException
     */
    public static void sendForDownloadStream(ChannelHandlerContext ctx, File file, String type, String fileName) throws IOException {
        //一个基础的OK请求
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        //handlers添加文件长度
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        //todo 如果为preview(浏览),则告诉浏览器,这个是PDF文件或其他文件,看业务,让其用自带插件浏览而非下载PDF(文件主要是PDF为浏览)
        if (type.equals("preview")) {
            response.headers().set(CONTENT_TYPE, "application/pdf; charset=utf-8");
        } else {
            //todo 处理非浏览类文件
            switch (type) {
                //svg格式文件
                case "svg":
                    response.headers().set(CONTENT_TYPE, " Image/svg+xml; charset=utf-8");
                    break;
                //视频
                case "video":
                    response.headers().set(CONTENT_TYPE, " video/mp4; charset=utf-8");
                    break;
                //剩下的，默认下载流
                default:
                    response.headers().set(CONTENT_TYPE, " application/octet-stream; charset=utf-8");
                    break;
            }
            //设定为： 以附件的形式下载， 文件名是UTF-8，作为转换
            String disposition = "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "utf-8");
            response.headers().add("Content-Disposition", disposition);
        }
        //todo 组装一些需要然前端知道的参数
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
