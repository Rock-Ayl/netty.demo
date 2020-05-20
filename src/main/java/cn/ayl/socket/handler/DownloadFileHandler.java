package cn.ayl.socket.handler;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.config.Const;
import cn.ayl.handler.FileHandler;
import cn.ayl.util.HttpUtils;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * created by Rock-Ayl on 2019-11-18
 * 下载处理器
 */
public class DownloadFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    protected static Logger logger = LoggerFactory.getLogger(DownloadFileHandler.class);

    /**
     * 请求进入点
     *
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        //从get请求获取参数
        Map<String, Object> map = HttpUtils.getParams(request, null);
        //根据请求路径抽取参数, 文件请求类型,文件fileId,文件名,用户cookieId
        FileRequestType type = FileRequestType.parse((String) map.get(Const.Type));
        String fileId = (String) map.get(Const.FileId);
        String fileName = (String) map.get(Const.FileName);
        String cookieId = (String) map.get(Const.CookieId);
        //从业务中读取文件
        File file = FileHandler.instance.readDownloadFile(type, fileId, fileName, cookieId);
        //读取失败，返回
        if (file == null) {
            //响应失败
            ResponseHandler.sendMessageOfJson(ctx, NOT_FOUND, "下载请求失败,文件不存在或用户信息失效.");
            return;
        }
        try {
            //响应成功
            ResponseHandler.sendFileStream(ctx, request, file, type);
        } catch (Exception e) {
            logger.error("响应请求文件流失败:{}", e);
        } finally {
            return;
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
            //响应
            ResponseHandler.sendMessageOfJson(ctx, INTERNAL_SERVER_ERROR, "下载请求异常，连接断开.");
            logger.error("下载请求异常，连接断开.");
        }
    }

}
