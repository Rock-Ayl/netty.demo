package cn.ayl.socket.encoder;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.common.json.JsonObject;
import cn.ayl.config.Const;
import cn.ayl.socket.handler.FileSendHandler;
import cn.ayl.util.DateUtils;
import cn.ayl.util.HttpUtils;
import cn.ayl.util.MD5Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created By Rock-Ayl 2019-11-14
 * 协议编码器及响应处理器
 */
public class ResponseAndEncoderHandler {

    protected static Logger logger = LoggerFactory.getLogger(ResponseAndEncoderHandler.class);

    /**
     * 构造
     *
     * @return
     */
    public static ResponseAndEncoderHandler use() {
        return new ResponseAndEncoderHandler();
    }

    /**
     * 私有化
     */
    private ResponseAndEncoderHandler() {

    }

    /**
     * 设置响应通用headers
     *
     * @param response
     */
    private void setServerHeaders(HttpResponse response) {
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        headers.set(HttpHeaderNames.SERVER, Const.ServerName);
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "origin,accept,cookieId,authorization,DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,content-length");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, 86400);
    }

    /**
     * 响应一般http请求并返回object
     *
     * @param ctx
     * @param status
     * @param result 返回结果,一般为Json
     */
    public void sendObject(ChannelHandlerContext ctx, HttpResponseStatus status, Object result) {
        //创建一个新缓冲
        ByteBuf content = Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        //请求初始化
        FullHttpResponse response = new DefaultFullHttpResponse(Const.CurrentHttpVersion, status, content);
        //判空
        if (content != null) {
            //组装content_type
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            //组装content_length
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        //添加通用参数
        setServerHeaders(response);
        //响应并关闭通道
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 响应http并返回Json,包含 Fail Message
     *
     * @param ctx
     * @param status
     * @param content
     */
    public void sendFailAndMessage(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        sendObject(ctx, status, JsonObject.Fail(content));
    }

    /**
     * 响应预检请求
     *
     * @param ctx
     */
    public void sendOption(ChannelHandlerContext ctx) {
        sendObject(ctx, HttpResponseStatus.OK, "ok");
    }

    /**
     * 响应并返回请求下载文件的文件流
     *
     * @param ctx              netty通道
     * @param range            断点续传范围
     * @param file             文件本体
     * @param randomAccessFile 文件本体
     * @param fileRequestType  文件请求类型(下载、预览)等等
     * @param realFileName     返回给对方的真实文件名
     * @throws IOException
     */
    public void sendFileStream(ChannelHandlerContext ctx, String range, File file, RandomAccessFile randomAccessFile, FileRequestType fileRequestType, String realFileName) throws IOException {
        //获取真实的文件后缀
        String realFileExt = FilenameUtils.getExtension(realFileName);
        //文件长度
        long fileLength = randomAccessFile.length();
        //当前时间
        long thisTime = System.currentTimeMillis();
        //国际标准文件最后修改时间
        String fileLastModified = DateUtils.SDF_HTTP_DATE_FORMATTER.format(new Date(file.lastModified()));
        //初始化一个200请求
        HttpResponse response = new DefaultHttpResponse(Const.CurrentHttpVersion, HttpResponseStatus.OK);
        //文件起始字节位置初始化
        long startOffset = 0;
        //文件结束字节位置初始化
        long endOffset = fileLength - 1;
        //传输文件的实际总长度
        long endLength = fileLength;
        //Range判空
        if (StringUtils.isNotEmpty(range)) {
            //设置为分片下载状态(由200->206)
            response.setStatus(HttpResponseStatus.PARTIAL_CONTENT);
            //解析Range前后区间
            String[] r = range.replace("bytes=", "").split("-");
            //设置文件起始字节位置
            startOffset = Long.parseLong(r[0]);
            //判断是否存在文件结束字节位置
            if (r.length == 2) {
                //文件结束字节位置
                endOffset = Long.parseLong(r[1]);
            }
            //设置响应范围
            response.headers().set(HttpHeaderNames.CONTENT_RANGE, HttpHeaderValues.BYTES + " " + startOffset + "-" + endOffset + "/" + fileLength);
            //传输文件的实际总长度
            endLength = endOffset - startOffset + 1;
        }
        //初始化文件类型
        String contentType;
        //设定化内容处理:以附件的形式下载、文件名、编码
        String disposition;
        //指定缓存机制
        String cacheControl;
        //根据文件请求类型设置headers
        switch (fileRequestType) {
            //只是下载
            case download:
                //所有下载都是流
                contentType = "application/octet-stream; charset=utf-8";
                //告诉浏览器是下载
                disposition = "attachment";
                break;
            //只是预览
            case preview:
            default:
                //按文件类别区分文件类型
                contentType = HttpUtils.parseHttpResponseContentType(realFileName);
                //告诉浏览器是预览
                disposition = "inline";
                break;
        }
        //根据文件后缀操作设置headers
        switch (realFileExt) {
            case "html":
                //设置必须资源效验
                cacheControl = "no-cache";
                //文件实体标签,用于效验文件未修改性
                response.headers().set(HttpHeaderNames.ETAG, MD5Utils.getFileMd5(file));
                break;
            case "js":
            case "css":
                //设置缓存时间为1年
                cacheControl = "max-age=31536000";
                //设置文件最后修改时间
                response.headers().set(HttpHeaderNames.LAST_MODIFIED, fileLastModified);
                break;
            default:
                //设置缓存时间为1天
                cacheControl = "max-age=86400";
                //设置文件最后修改时间
                response.headers().set(HttpHeaderNames.LAST_MODIFIED, fileLastModified);
                break;
        }
        //支持告诉客户端支持分片下载,如迅雷等多线程
        response.headers().set(HttpHeaderNames.ACCEPT_RANGES, HttpHeaderValues.BYTES);
        //handlers添加文件实际传输长度
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, endLength);
        //文件内容类型
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        //指定缓存机制
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, cacheControl);
        //文件名,是否 save as
        response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, disposition + "; filename*=UTF-8''" + URLEncoder.encode(realFileName, "utf-8"));
        //该资源发送的时间
        response.headers().set(HttpHeaderNames.DATE, DateUtils.SDF_HTTP_DATE_FORMATTER.format(thisTime));
        //添加通用参数
        setServerHeaders(response);
        //写入响应及对应响应报文
        ctx.write(response);
        //文件传输过程
        ChannelFuture sendFileFuture;
        //判断是否为https
        if (isHttps(ctx)) {
            //https的传输文件方式,非零拷贝,低效,不推荐
            sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(randomAccessFile, startOffset, endLength, 8192)), ctx.newProgressivePromise());
        } else {
            //http默认的传输文件方式,零拷贝,高效
            sendFileFuture = ctx.writeAndFlush(new DefaultFileRegion(randomAccessFile.getChannel(), startOffset, endLength), ctx.newProgressivePromise());
        }
        //添加传输监控
        sendFileFuture.addListener(FileSendHandler.VOID(file.getName()));
        //ctx响应并关闭(如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成)
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    /**
     * 判断一个请求是否为https即拥有SSL
     *
     * @param ctx
     * @return
     */
    private boolean isHttps(ChannelHandlerContext ctx) {
        if (ctx.pipeline().get(SslHandler.class) != null) {
            return true;
        }
        return false;
    }

}
