package cn.ayl.socket.handler;

import cn.ayl.common.enumeration.FileRequestType;
import cn.ayl.config.Const;
import cn.ayl.handler.FileHandler;
import cn.ayl.socket.encoder.ResponseAndEncoderHandler;
import cn.ayl.util.HttpUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * created by Rock-Ayl on 2019-11-18
 * 下载处理器
 */
public class DownloadFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    protected static Logger logger = LoggerFactory.getLogger(DownloadFileHandler.class);

    //静态资源文件流
    private RandomAccessFile randomAccessFile;

    /**
     * 请求进入点
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
        try {
            //初始化文件流
            this.randomAccessFile = new RandomAccessFile(file, "r");
            //如果成功获取文件
            if (file != null && file.exists() && file.isFile()) {
                //获取range值
                String range = request.headers().get(HttpHeaderNames.RANGE);
                //响应成功
                ResponseAndEncoderHandler.use().sendFileStream(ctx, range, file, this.randomAccessFile, type, fileName);
            } else {
                //响应失败
                ResponseAndEncoderHandler.use().sendFailAndMessage(ctx, NOT_FOUND, "下载请求失败,文件不存在或用户信息失效.");
            }
        } catch (Exception e) {
            logger.error("响应请求文件流失败:{}", e);
        }
    }

    /**
     * 异常抓取
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //输入日志
        logger.warn("下载请求异常,连接断开,异常为:" + cause);
        //当连接断开的时候 关闭未关闭的文件流
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                //输入日志
                logger.error("下载请求关闭文件流异常:" + cause);
            }
        }
        ctx.close();
    }

}
