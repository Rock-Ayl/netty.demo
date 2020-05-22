package cn.ayl.socket.handler;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.handler.FileHandler;
import cn.ayl.socket.encoder.ResponseAndEncoderHandler;
import cn.ayl.util.DateUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import jodd.io.FileNameUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * created by Rock-Ayl on 2019-11-26
 * 静态资源处理器
 */
public class ResourceHandler {

    protected static Logger logger = LoggerFactory.getLogger(ResourceHandler.class);

    /**
     * 处理静态文件资源
     *
     * @param ctx
     * @param req
     * @param uriPath
     */
    public void handleResource(ChannelHandlerContext ctx, HttpRequest req, String uriPath) {
        try {
            //判空
            if (StringUtils.isNotEmpty(uriPath)) {
                //解析文件名称
                String fileBaseName = FileNameUtil.getBaseName(uriPath);
                //解析文件后缀
                String fileExt = FileNameUtil.getExtension(uriPath);
                //解码并组装成静态文件path
                String resourceFilePath = URLDecoder.decode(fileBaseName, "UTF-8") + "." + fileExt;
                //获取服务器中的静态文件
                File file = FileHandler.instance.readResourceFile(resourceFilePath);
                //如果是个文件
                if (file.exists() && file.isFile()) {
                    //如果静态文件没有改动,直接返回(让浏览器用缓存)
                    if (isNotModified(req, file)) {
                        //文件未被修改,浏览器可以延用缓存
                        ResponseAndEncoderHandler.sendMessageOfJson(ctx, HttpResponseStatus.NOT_MODIFIED, "Modified false.");
                    } else {
                        //响应请求文件流
                        ResponseAndEncoderHandler.sendFileStream(ctx, req, file, FileRequestType.preview);
                    }
                } else {
                    //不存在文件,响应失败
                    ResponseAndEncoderHandler.sendMessageOfJson(ctx, HttpResponseStatus.NOT_FOUND, "没有发现文件.");
                }
            }
        } catch (IOException e) {
            logger.error("handleResource IOException.", e);
        }
    }

    /**
     * 检测静态文件是否没有修改过
     * 1.修改过,浏览器就必须重新刷新文件
     * 2.未修改过,浏览器延用缓存
     *
     * @param req  请求
     * @param file 服务器文件
     * @return
     */
    private boolean isNotModified(HttpRequest req, File file) {
        //默认修改过
        boolean notModified = false;
        //获取请求给与的浏览器缓存的文件最后修改时间
        String ifModifiedSince = req.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        try {
            //如果存在文件最后修改时间
            if (StringUtils.isNotEmpty(ifModifiedSince)) {
                //转化为时间戳并变为秒
                long ifModifiedSinceDateSeconds = DateUtils.SDF_HTTP_DATE_FORMATTER.parse(ifModifiedSince).getTime();
                //获取服务器静态文件最后修改时间
                long fileLastModifiedSeconds = file.lastModified();
                //如果相同
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    //浏览器不需要修改缓存静态文件
                    notModified = true;
                }
            }
        } catch (Exception e) {
            logger.error("检测缓存文件是否修改出错:{}", e);
        } finally {
            return notModified;
        }
    }

}
