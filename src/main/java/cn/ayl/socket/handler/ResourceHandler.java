package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import jodd.io.FileNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static io.netty.handler.codec.http.HttpHeaderNames.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * created by Rock-Ayl on 2019-11-26
 * 静态资源处理器
 */
public class ResourceHandler {

    protected static Logger logger = LoggerFactory.getLogger(ResourceHandler.class);

    //静态资源-文件最后修改时间格式
    public static final SimpleDateFormat HTTP_DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    //资源处理器
    public void handleResource(ChannelHandlerContext ctx, HttpRequest req) {
        //获取请求静态路径
        String uri = req.getUri();
        //如果uri长度太短，直接返回
        if (uri.length() == 0) {
            return;
        }
        try {
            //对中文进行解码
            uri = URLDecoder.decode(FileNameUtil.getBaseName(uri), "UTF-8") + "." + FileNameUtil.getExtension(uri);
        } catch (UnsupportedEncodingException e) {
            logger.error("Resource decode Exception.", e);
            return;
        }
        //获取服务器中的文件
        File file = new File(Const.ResourcePath + uri);
        //文件是否存在
        boolean hasFile = file.exists();
        //如果存在，处理文件
        if (hasFile) {
            try {
                //处理文件
                handleFile(ctx, req, file, uri);
            } catch (Exception e) {
                logger.error("Resource HandleFile Exception.", e);
                return;
            }
        } else {
            //不存在文件，直接返回 not found
            ResponseHandler.sendMessage(ctx, NOT_FOUND, uri);
        }
    }

    private void handleFile(ChannelHandlerContext ctx, HttpRequest req, File file, String uri) throws Exception {
        //如果文件不需要修改，返回
        if (!isModified(ctx, req, file, uri)) {
            return;
        }
        //需要修改，响应请求文件流
        ResponseHandler.sendStream(ctx, req, file);
    }

    /**
     * 检测缓存文件是否修改
     *
     * @param ctx
     * @param req
     * @param f
     * @param uri
     * @return
     */
    private boolean isModified(ChannelHandlerContext ctx, HttpRequest req, File f, String uri) {
        //获取文件最后修改时间
        String ifModifiedSince = req.headers().get(IF_MODIFIED_SINCE);
        try {
            //如果存在文件最后修改时间
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                //转化为时间戳并变为秒
                long ifModifiedSinceDateSeconds = HTTP_DATE_FORMATTER.parse(ifModifiedSince).getTime() / 1000;
                //获取服务器文件最后修改时间
                long fileLastModifiedSeconds = f.lastModified() / 1000;
                //如果相同，告诉浏览器不需要修改
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    ResponseHandler.sendMessage(ctx, NOT_MODIFIED, uri);
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

}
