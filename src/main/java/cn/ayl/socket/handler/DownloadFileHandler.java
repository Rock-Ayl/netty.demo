package cn.ayl.socket.handler;

import cn.ayl.util.json.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * created by Rock-Ayl on 2019-11-18
 * todo 下载处理器
 */
@ChannelHandler.Sharable
public class DownloadFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    protected static Logger logger = LoggerFactory.getLogger(DownloadFileHandler.class);

    static class DownloadInput implements ChunkedInput<HttpContent> {

        private final long startOffset;
        private final long endOffset;
        private final int chunkSize;
        private long offset;
        private InputStream stream;
        private long length;

        public DownloadInput(InputStream stream, long length) throws IOException {
            this(stream, 0, length, 8196);
        }

        public DownloadInput(InputStream stream, long offset, long length, int chunkSize) throws IOException {
            this.stream = stream;
            this.offset = startOffset = offset;
            this.length = length;
            endOffset = offset + length;
            this.chunkSize = chunkSize;
            stream.skip(offset);
        }


        @Override
        public boolean isEndOfInput() {
            return offset >= endOffset;
        }

        @Override
        public void close() throws Exception {
            stream.close();
        }

        @Override
        public HttpContent readChunk(ChannelHandlerContext ctx) throws Exception {
            return readChunk(ctx.alloc());
        }

        @Override
        public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
            if (offset >= length) {
                return null;
            }
            int size = (int) Math.min(this.chunkSize, endOffset - offset);
            boolean release = true;
            ByteBuf buf = allocator.heapBuffer(size);
            try {
                stream.read(buf.array(), buf.arrayOffset(), size);
                buf.writerIndex(chunkSize);
                this.offset += size;
                release = false;
                return new DefaultHttpContent(buf);
            } finally {
                if (release) {
                    buf.release();
                }
            }
        }

        @Override
        public long length() {
            return endOffset - startOffset;
        }

        @Override
        public long progress() {
            return offset - startOffset;
        }
    }

    protected void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        JsonObject result = JsonObject.Success();
        result.append("message", content);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(result.toJson(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    protected InputStream readDownloadStream(String type, String fileId, String fileName) {
        //todo 读取下载流逻辑
        File file = new File("/workspace/保密码.doc");
        InputStream stream = null;
        try {
            stream = FileUtils.openInputStream(file);
        } catch (IOException e) {
            logger.error("文件不存在,Error:{}", e);
        } finally {
            return stream;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //获取get请求路径的参数
        Map map = getGetParamsFromChannel(request);
        //根据请求路径抽取参数
        String type = (String) map.get("type");
        String fileId = (String) map.get("fileId");
        String fileName = (String) map.get("fileName");
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(fileId)) {
            logger.error("下载请求失败.");
            sendError(ctx, NOT_FOUND, "下载文件参数必须同时包含type&fileId&fileName");
            return;
        }
        InputStream stream = readDownloadStream(type, fileId, fileName);
        if (stream == null) {
            logger.error("下载请求失败.");
            sendError(ctx, NOT_FOUND, "文件不存在");
            return;
        }
        try {
            long fileLength = stream.available();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(response, fileLength);
            if (type.equals("preview")) {
                response.headers().set(CONTENT_TYPE, "application/pdf; charset=utf-8");
            } else {
                switch (type) {
                    case "svg":
                        response.headers().set(CONTENT_TYPE, " Image/svg+xml; charset=utf-8");
                        break;
                    case "video":
                        response.headers().set(CONTENT_TYPE, " video/mp4; charset=utf-8");
                        break;
                    default:
                        response.headers().set(CONTENT_TYPE, " application/octet-stream; charset=utf-8");
                        break;
                }
                if (type.equals("pdf")) {
                    String fileExt = FilenameUtils.getExtension(fileName);
                    fileName = fileName.replace(fileExt, "pdf");
                }
                if (fileId.split(",").length >= 2) {
                    fileName = fileName.split(",")[0];
                    String fileExt = FilenameUtils.getExtension(fileName);
                    fileName = fileName.replace(fileExt, "zip");
                }
                String disposition = "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "utf-8");
                response.headers().add("Content-Disposition", disposition);
            }
            response.headers().add("access-control-allow-origin", "*");
            response.headers().add("access-control-allow-credentials", true);
            response.headers().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.headers().add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type,Content-Length,cookieId,FileName,FileType,FileSize,FileDate,FileParam");
            response.headers().add("Access-Control-Max-Age", 86400);
            ctx.write(response);
            ChannelFuture sendFuture = ctx.write(new HttpChunkedInput(new ChunkedStream(stream, 8 * 1024)), ctx.newProgressivePromise());
            sendFuture.addListener(new ChannelProgressiveFutureListener() {

                @Override
                public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                    stream.close();
                }

                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {

                }
            });
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (Exception e) {
            logger.error("type=" + type + "&fileId=" + fileId, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR, "连接断开");
            logger.error("下载请求了解断开.");
        }
    }

    /**
     * Http获取GET方式传递的参数
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
                //强转并组装
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        } else {
            return null;
        }
    }

}
