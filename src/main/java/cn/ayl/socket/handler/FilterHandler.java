package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.socket.rpc.Context;
import cn.ayl.socket.decoder.ProtocolDecoder;
import cn.ayl.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * created by Rock-Ayl 2019-12-11
 * 过滤器,用来处理一些基础的东西，如请求类型分配、验证身份、解决连接重用bug等
 */
public class FilterHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FilterHandler.class);

    //上下文
    private Context context;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //获取请求
        context = ctx.channel().attr(Const.AttrContext).get();
        //
        if (context == null) {
            return;
        }
        //初始化
        if (filter(ctx, msg)) {
            //向下个节点传递
            ctx.fireChannelRead(msg);
        } else {
            //释放请求
            ReferenceCountUtil.safeRelease(msg);
        }
    }

    //过滤器初始化
    private boolean filter(ChannelHandlerContext ctx, Object msg) {
        //过滤下http请求
        if (msg instanceof HttpRequest) {
            //强转
            HttpRequest req = (HttpRequest) msg;
            //判断请求类型是否为预检
            if (req.method().name().equalsIgnoreCase("OPTIONS")) {
                context.requestType = Const.RequestType.http;
                //响应预检
                ResponseHandler.sendOption(ctx);
                return false;
            }
            //获得请求path
            context.uriPath = getPath(req);
            //分配请求类型
            getHttpRequestType();
            //解决长连接重用与短连接404问题
            checkChanelPipe(ctx);
            //身份效验
            if (!auth(req)) {
                ResponseHandler.sendMessageForJson(ctx, HttpResponseStatus.UNAUTHORIZED, "身份验证失败.");
                return false;
            }
        }
        return true;
    }

    /**
     * 当有多个长链接和短连接时,netty会只调用一次解码器,造成请求解码重用,导致各种类型的请求404,这里会处理重用问题
     */
    private void checkChanelPipe(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        switch (context.requestType) {
            case download:
                logger.error("Filter. checkChanelPipe download ctx.type={}", context.requestType);
                //清除http处理器
                ProtocolDecoder.clearHttpHandlerAddLast(p);
                //增加下载套件及处理器
                ProtocolDecoder.downloadAddLast(p);
                break;
            case service:
                logger.info("Filter. checkChanelPipe service ctx.type={}", context.requestType);
                //清除下载套件及处理器
                ProtocolDecoder.clearDownloadAddLast(p);
                //增加http处理器
                ProtocolDecoder.httpHandlerAddLast(p);
                break;
        }
    }

    //身份效验
    private boolean auth(HttpRequest req) {
        //是否需要验证
        boolean needAuth = false;
        //根据请求类型分类是否需要验证身份
        switch (context.requestType) {
            case upload:
            case download:
                needAuth = true;
                break;
            case service:
                //service的得拿到 auth
                needAuth = HttpHandler.hasNeedAuth(req);
                break;
        }
        //如果Cookie需要认证(auto = true)
        if (needAuth) {
            //todo 验证失败条件,然后修改成返回false
            if (true) {
                return true;
            }
        }
        return true;
    }

    /***
     * 根据路径开头分配Http请求内容
     *
     * upload开头是上传
     * htmlPage开头是请求静态资源
     * 如果有文件后缀是资源
     * 什么都不是，默认看做服务
     * @return
     */
    private void getHttpRequestType() {
        String path = context.uriPath;
        if (path.startsWith(Const.UploadPath)) {
            context.requestType = Const.RequestType.upload;
        } else if (path.startsWith(Const.HttpPagePath)) {
            context.requestType = Const.RequestType.htmlPage;
        } else if (path.startsWith(Const.DownloadPath)) {
            context.requestType = Const.RequestType.download;
        } else if (!StringUtils.isEmpty(FilenameUtils.getExtension(path))) {
            context.requestType = Const.RequestType.resource;
        } else {
            context.requestType = Const.RequestType.service;
        }
    }

    /**
     * Http获取请求Path
     *
     * @param req
     * @return
     */
    private String getPath(HttpRequest req) {
        String path = null;
        try {
            path = new URI(req.getUri()).getPath();
        } catch (Exception e) {
            logger.error("接口解析错误.");
        } finally {
            return path;
        }
    }

}
