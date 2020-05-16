package cn.ayl.socket.handler;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.config.Const;
import cn.ayl.handler.FileHandler;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
        //获取get请求的参数
        Map map = getGetParamsFromChannel(request);
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
            ResponseHandler.sendFileStream(ctx, file, type);
        } catch (Exception e) {
            logger.error("响应请求文件流失败:{}", e);
        } finally {
            return;
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
            ResponseHandler.sendMessageOfJson(ctx, INTERNAL_SERVER_ERROR, "下载请求异常，连接断开.");
            logger.error("下载请求异常，连接断开.");
        }
    }

}
