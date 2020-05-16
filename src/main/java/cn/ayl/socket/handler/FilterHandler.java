package cn.ayl.socket.handler;

import cn.ayl.common.db.redis.Redis;
import cn.ayl.common.enumeration.RequestType;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonUtil;
import cn.ayl.config.Const;
import cn.ayl.socket.rpc.Context;
import cn.ayl.socket.decoder.ProtocolDecoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by Rock-Ayl 2019-12-11
 * 过滤器,用来处理一些基础的东西，如请求类型分配、验证身份、解决连接重用bug等
 */
public class FilterHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FilterHandler.class);

    //请求上下文
    private Context context;

    /**
     * 过滤器初始化
     *
     * @param ctx
     * @param msg
     * @return
     */
    private boolean filter(ChannelHandlerContext ctx, Object msg) {
        //过滤下http请求
        if (msg instanceof HttpRequest) {
            //强转
            HttpRequest req = (HttpRequest) msg;
            //判断请求类型是否为预检
            if (req.method().name().equalsIgnoreCase("OPTIONS")) {
                //预检请求当做普通http
                this.context.requestType = RequestType.http;
                //响应预检
                ResponseHandler.sendOption(ctx);
                return false;
            }
            //获取请求cookieId
            this.context.cookieId = req.headers().get(Const.CookieId, "");
            //获得请求path
            this.context.uriPath = req.uri();
            //分配请求类型
            this.context.requestType = getHttpRequestType(context.uriPath);
            //解决长连接重用与短连接404问题
            checkChanelPipe(ctx);
            //身份效验
            if (!authUser(req)) {
                //如果身份效验失败,直接发送错误信息
                ResponseHandler.sendMessageOfJson(ctx, HttpResponseStatus.UNAUTHORIZED, "身份验证失败.");
                return false;
            }
        }
        return true;
    }

    /**
     * 当有多个长链接和短连接时,netty会只调用一次解码器,造成请求解码重用,导致各种类型的请求404,这里统一处理重用问题
     */
    private void checkChanelPipe(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        //根据真正的请求类型分配
        switch (context.requestType) {
            case download:
                //清除http处理器
                ProtocolDecoder.clearHttpHandlerAddLast(p);
                //增加下载套件及处理器
                ProtocolDecoder.downloadAddLast(p);
                break;
            case service:
                //清除下载套件及处理器
                ProtocolDecoder.clearDownloadAddLast(p);
                //增加http处理器
                ProtocolDecoder.httpHandlerAddLast(p);
                break;
        }
    }

    /**
     * 效验请求的用户身份
     *
     * @param req
     * @return
     */
    private boolean authUser(HttpRequest req) {
        //是否需要验证
        boolean needAuth = false;
        //根据请求类型分类是否需要验证身份
        switch (context.requestType) {
            //上传必须验证身份
            case upload:
                needAuth = true;
                break;
            //服务需要看接口设置的auth
            case service:
                //获取接口auth
                needAuth = HttpHandler.hasNeedAuth(req);
                break;
        }
        //如果Cookie需要认证(auto = true)
        if (needAuth) {
            //获取cookieId
            String cookieId = context.cookieId;
            //判空
            if (StringUtils.isNotBlank(cookieId)) {
                //从Redis中获取用户登录信息并解析成Json
                JsonObject userInfo = JsonUtil.parse(Redis.user.get(cookieId));
                //获取用户id
                long userId = userInfo.getLong("userId", 0L);
                //如果是真实用户id
                if (userId != 0L) {
                    //赋予上下文用户id
                    this.context.ctxUserId = userId;
                    //验证成功
                    return true;
                }
            }
            //默认失败
            return false;
        }
        //不需要验证默认验证成功
        return true;
    }

    /**
     * 根据path分配请求内容
     * <p>
     * 1.upload开头,视为上传文件请求
     * 2.download开头,视为下载文件请求
     * 3.htmlPage开头,视为页面请求
     * 4.文件后缀结尾,视为静态文件资源请求
     * 5.默认看做接口服务
     * </p>
     *
     * @param path
     * @return
     */
    private RequestType getHttpRequestType(String path) {
        if (path.startsWith(Const.UploadPath)) {
            return RequestType.upload;
        } else if (path.startsWith(Const.DownloadPath)) {
            return RequestType.download;
        } else if (path.startsWith(Const.HttpPagePath)) {
            return RequestType.htmlPage;
        } else if (StringUtils.isNotEmpty(FilenameUtils.getExtension(path))) {
            return RequestType.resource;
        } else {
            return RequestType.service;
        }
    }

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

}
