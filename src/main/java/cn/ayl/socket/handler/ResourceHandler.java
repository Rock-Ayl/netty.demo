package cn.ayl.socket.handler;

import cn.ayl.handler.FileHandler;
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

    /**
     * 读取服务器中静态资源
     *
     * @param pathSuffix 资源path后缀
     * @return 文件
     */
    private File readResourceFile(String pathSuffix) {
        return FileHandler.instance.readResourceFile(pathSuffix);
    }

    //资源处理器
    public void handleResource(ChannelHandlerContext ctx, HttpRequest req) {
        //获取请求静态路径后缀
        String pathSuffix = req.getUri();
        //如果资源后缀长度太短，直接返回
        if (pathSuffix.length() == 0) {
            return;
        }
        try {
            //对中文进行解码
            pathSuffix = URLDecoder.decode(FileNameUtil.getBaseName(pathSuffix), "UTF-8") + "." + FileNameUtil.getExtension(pathSuffix);
        } catch (UnsupportedEncodingException e) {
            logger.error("Resource decode Exception.", e);
            return;
        }
        //获取服务器中的文件
        File file = readResourceFile(pathSuffix);
        //文件是否存在
        boolean hasFile = file.exists();
        //如果存在，处理文件
        if (hasFile) {
            try {
                //处理文件
                handleFile(ctx, req, file);
            } catch (Exception e) {
                logger.error("Resource HandleFile Exception.", e);
                return;
            }
        } else {
            //不存在文件，直接返回 not found
            ResponseHandler.sendMessageForJson(ctx, NOT_FOUND, "没有发现文件.");
        }
    }

    private void handleFile(ChannelHandlerContext ctx, HttpRequest req, File file) throws Exception {
        //如果文件不需要修改，返回
        if (!isModified(ctx, req, file)) {
            return;
        }
        //需要修改，响应请求文件流
        ResponseHandler.sendForResourceStream(ctx, file);
    }

    /**
     * 检测缓存文件是否修改
     *
     * @param ctx
     * @param req
     * @param file
     * @return
     */
    private boolean isModified(ChannelHandlerContext ctx, HttpRequest req, File file) {
        //获取文件最后修改时间
        String ifModifiedSince = req.headers().get(IF_MODIFIED_SINCE);
        try {
            //如果存在文件最后修改时间
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                //转化为时间戳并变为秒
                long ifModifiedSinceDateSeconds = HTTP_DATE_FORMATTER.parse(ifModifiedSince).getTime() / 1000;
                //获取服务器文件最后修改时间
                long fileLastModifiedSeconds = file.lastModified() / 1000;
                //如果相同，告诉浏览器不需要修改
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    //不需要修改
                    ResponseHandler.sendMessageForJson(ctx, NOT_MODIFIED, "Modified false.");
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

}
