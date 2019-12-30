package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.util.StringUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
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
     * todo 读取下载流逻辑,现在没有业务，随意写了一个，以后可以添加身份
     * 读取业务中的文件
     *
     * @param type
     * @param fileId
     * @param fileName
     * @return
     */
    protected File readDownloadFile(String type, String fileId, String fileName) {
        File file = new File(Const.DownloadFilePath + fileName);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * 请求进入点
     *
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //获取get请求路径的参数
        Map map = getGetParamsFromChannel(request);
        //todo 根据请求路径抽取参数，可以更多
        String type = (String) map.get(Const.Type);
        String fileId = (String) map.get(Const.FileId);
        String fileName = (String) map.get(Const.FileName);
        //todo 这里可以补充逻辑判定
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(fileId) || StringUtil.isEmpty(fileName)) {
            logger.error("下载请求失败.");
            ResponseHandler.sendMessageForJson(ctx, NOT_FOUND, "下载文件参数必须同时包含type&fileId&fileName");
            return;
        }
        //从业务中读取文件
        File file = readDownloadFile(type, fileId, fileName);
        //读取失败，返回
        if (file == null) {
            logger.error("下载请求失败,文件不存在.");
            ResponseHandler.sendMessageForJson(ctx, NOT_FOUND, "下载请求失败,文件不存在.");
            return;
        }
        try {
            //响应成功
            ResponseHandler.sendForDownloadStream(ctx, file, type, fileName);
        } catch (Exception e) {
            logger.error("type=" + type + "&fileId=" + fileId, e);
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
            ResponseHandler.sendMessageForJson(ctx, INTERNAL_SERVER_ERROR, "下载请求异常，连接断开.");
            logger.error("下载请求异常，连接断开.");
        }
    }

}
