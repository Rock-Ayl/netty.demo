package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.util.StringUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import org.apache.commons.io.FileUtils;
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
 * 下载处理器
 */
public class DownloadFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    protected static Logger logger = LoggerFactory.getLogger(DownloadFileHandler.class);

    /**
     * todo 读取下载流逻辑,现在没有业务，随意写了一个，以后可以添加身份
     * 读取业务中的文件流
     *
     * @param type
     * @param fileId
     * @param fileName
     * @return
     */
    protected InputStream readDownloadStream(String type, String fileId, String fileName) {
        File file = new File("/Volumes/本机机械硬盘/世界欠我一个初恋-24.mp4");
        InputStream stream = null;
        try {
            stream = FileUtils.openInputStream(file);
        } catch (IOException e) {
            logger.error("文件不存在,Error:{}", e);
        } finally {
            return stream;
        }
    }

    /**
     * 请求接入点
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //获取get请求路径的参数
        Map map = getGetParamsFromChannel(request);
        //todo 根据请求路径抽取参数，可以更多
        String type = (String) map.get("type");
        String fileId = (String) map.get("fileId");
        String fileName = (String) map.get("fileName");
        //todo 这里可以补充逻辑判定
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(fileId) || StringUtil.isEmpty(fileName)) {
            logger.error("下载请求失败.");
            ResponseHandler.sendMessage(ctx, NOT_FOUND, "下载文件参数必须同时包含type&fileId&fileName");
            return;
        }
        //从业务中读取文件
        InputStream stream = readDownloadStream(type, fileId, fileName);
        //读取失败，返回
        if (stream == null) {
            logger.error("下载请求失败,文件不存在.");
            ResponseHandler.sendMessage(ctx, NOT_FOUND, "下载请求失败,文件不存在.");
            return;
        }
        try {
            //文件流大小
            long fileLength = stream.available();
            //创建响应成功
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            //设置content-length
            HttpUtil.setContentLength(response, fileLength);
            //如果为preview(浏览)，则告诉浏览器，这个是PDF文件,让其用自带插件浏览而非下载PDF(文件主要是PDF为浏览)
            if (type.equals("preview")) {
                response.headers().set(CONTENT_TYPE, "application/pdf; charset=utf-8");
            } else {
                //处理非浏览类文件
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
            response.headers().add("access-control-allow-origin", "*");
            response.headers().add("access-control-allow-credentials", true);
            response.headers().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.headers().add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type,Content-Length,cookieId,fileName,fileId,type");
            response.headers().add("Access-Control-Max-Age", 86400);
            //请求写入ctx
            ctx.write(response);
            //写入文件流
            ChannelFuture sendFuture = ctx.write(new HttpChunkedInput(new ChunkedStream(stream, Const.ChunkSize)), ctx.newProgressivePromise());
            sendFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                    //操作完成后干掉文件内存
                    stream.close();
                }

                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {

                }
            });
            //响应并关闭
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (Exception e) {
            logger.error("type=" + type + "&fileId=" + fileId, e);
        }
    }

    /**
     * 异常抓取
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx.channel().isActive()) {
            ResponseHandler.sendMessage(ctx, INTERNAL_SERVER_ERROR, "下载请求异常，连接断开.");
            logger.error("下载请求异常，连接断开.");
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
