package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.rpc.Context;
import cn.ayl.util.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * created by Rock-Ayl 2019-12-11
 * 过滤器,用来处理一些基础的东西，如请求类型分配、验证身份等
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
            //身份效验
            if (!auth()) {
                ResponseHandler.sendMessageForJson(ctx, HttpResponseStatus.OK, "身份验证失败.");
                return false;
            }
        }
        return true;
    }

    //身份效验
    private boolean auth() {
        //是否需要验证
        boolean needAuth = false;
        //根据请求类型分类是否需要验证身份
        switch (context.requestType) {
            case upload:
            case download:
                needAuth = true;
                break;
            case service:
                //todo service的得拿到 auth
                break;
        }
        //如果Cookie需要认证(auto = true)
        if (needAuth) {
            //todo 验证失败
            if (false) {
                return false;
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
        } else if (!StringUtil.isEmpty(FilenameUtils.getExtension(path))) {
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
