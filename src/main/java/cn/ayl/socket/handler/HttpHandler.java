package cn.ayl.socket.handler;

import cn.ayl.pojo.Method;
import cn.ayl.pojo.Param;
import cn.ayl.pojo.Registry;
import cn.ayl.pojo.Service;
import cn.ayl.config.Const;
import cn.ayl.socket.encoder.ResponseAndEncoderHandler;
import cn.ayl.socket.rpc.Context;
import cn.ayl.util.HttpUtils;
import cn.ayl.util.ScanClassUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * created by Rock-Ayl on 2019-11-18
 * http请求处理器
 */
public class HttpHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    //请求上下文
    Context context;
    //上传处理器
    private UploadFileHandler uploadFileHandler;
    //静态资源处理器
    private ResourceHandler responseHandler;
    //静态资源文件流
    private RandomAccessFile randomAccessFile;

    /**
     * 根据service请求判断是否需要验证身份
     *
     * @return
     */
    public static boolean hasNeedAuth(HttpRequest req) {
        //默认需要验证
        boolean needAuth = true;
        try {
            //根据请求路径获得服务和方法名
            List<String> serviceAndMethod = getServiceAndMethod(req.uri());
            //判空
            if (serviceAndMethod.size() > 1) {
                //获取服务
                Service serviceEntry = Registry.serviceMap.get(serviceAndMethod.get(0));
                //如果服务存在
                if (serviceEntry != null) {
                    //获取请求类型
                    String command = req.method().toString().toLowerCase();
                    //如果是支持的请求类型
                    if (serviceEntry.commandMap.containsKey(command)) {
                        //获取该请求类型下的方法map
                        LinkedHashMap<String, Method> methodMap = serviceEntry.commandMap.get(command);
                        //获取服务中的方法
                        Method methodEntry = methodMap.get(serviceAndMethod.get(1));
                        //如果方法存在
                        if (methodEntry != null) {
                            //返回是否需要验证
                            needAuth = methodEntry.auth;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("请求是否需要验证十分失败:{}", e);
        } finally {
            //返回结果
            return needAuth;
        }
    }

    /**
     * 根据path和params处理业务并返回结果
     *
     * @param req
     * @return
     */
    private Object handleServiceFactory(HttpRequest req) {
        //根据请求路径获得服务和方法名
        List<String> serviceAndMethod = getServiceAndMethod(this.context.getUriPath());
        if (serviceAndMethod.size() < 2) {
            return Const.Json_No_Service;
        }
        //服务名
        String serviceName = serviceAndMethod.get(0);
        //判空
        if (StringUtils.isBlank(serviceName)) {
            return Const.Json_No_Service;
        }
        //获取服务
        Service serviceEntry = Registry.serviceMap.get(serviceName);
        //如果服务存在
        if (serviceEntry == null) {
            return Const.Json_No_Service;
        }
        //请求类型
        String command = req.method().toString().toLowerCase();
        //如果不存在于支持的请求类型
        if (!serviceEntry.commandMap.containsKey(command)) {
            return Const.Json_No_ContentType;
        }
        //获取服务中的方法
        Method methodEntry = serviceEntry.commandMap.get(command).get(serviceAndMethod.get(1));
        //如果方法存在
        if (methodEntry == null) {
            return Const.Json_No_InterFace;
        }
        //获取方法中的参数组
        LinkedHashMap<String, Param> paramMap = methodEntry.paramMap;
        List<String> paramList = methodEntry.paramList;
        //请求参数
        Map<String, Object> params;
        try {
            //解析请求参数
            params = HttpUtils.getParams(req, paramMap);
        } catch (Exception e) {
            logger.error("解析参数出现异常, error: [{}]", e);
            return Const.Json_Parse_Param_Find_Exception;
        }
        if (params == null) {
            return Const.Json_Error_Param;
        }
        //根据List遍历处理请求
        for (String paramKey : paramList) {
            //是否非必须该参数 false:必须 true:不必须
            Boolean optional = paramMap.get(paramKey).optional;
            //是否存在该值
            Boolean hasParam = params.containsKey(paramKey);
            //如果必须传并且参数中没有对应Key,回手掏
            if (optional == false && hasParam == false) {
                return Const.Json_Error_Param;
            }
        }
        //已确认服务接口参数均对应上,获取服务的实现类
        Class serviceClass = ScanClassUtils.findImplClass(serviceEntry.interFaceClass);
        //是否存在实现
        if (serviceClass == null) {
            return Const.Json_No_Impl;
        }
        try {
            //调用构造函数
            Constructor noArgConstructor = serviceClass.getDeclaredConstructor();
            //实现类
            Object service = noArgConstructor.newInstance();
            //如果服务继承了上下文
            if (service instanceof Context) {
                //赋予用户信息
                ((Context) service).setUser(this.context.getUser());
            }
            //组装参数和参数类型
            Object[] valueArr = new Object[paramList.size()];
            Class<?>[] valueTypeArr = new Class[paramList.size()];
            for (int i = 0; i < paramList.size(); i++) {
                //参数类型
                Class<?> parType = methodEntry.paramMap.get(paramList.get(i)).clazz;
                String key = paramList.get(i);
                //组装参数
                valueArr[i] = params.get(key);
                //组装类型
                valueTypeArr[i] = parType;
            }
            //定位服务的方法
            java.lang.reflect.Method method = serviceClass.getMethod(methodEntry.name, valueTypeArr);
            logger.info("service:[{}] Method:[{}]", serviceClass, methodEntry.name);
            //加入参数并执行
            Object resultObject = method.invoke(service, valueArr);
            //获取返回值
            return resultObject;
        } catch (Exception e) {
            logger.error("请求构建函数失败, error: [{}]", e);
            return Const.Json_Find_Exception;
        }
    }

    /**
     * 分配各类http请求内容处理
     *
     * @param ctx
     * @param req
     */
    private void handleHttpRequest(final ChannelHandlerContext ctx, HttpRequest req) {
        switch (this.context.getRequestType()) {
            //请求静态资源
            case resource:
                //创建一个静态资源处理器
                this.responseHandler = new ResourceHandler();
                //处理请求并记录文件流
                this.randomAccessFile = this.responseHandler.handleResource(ctx, req, this.context.getUriPath());
                break;
            //上传请求
            case upload:
                if (this.uploadFileHandler != null) {
                    //清除解码
                    this.uploadFileHandler.clearDecoder();
                }
                //创建一个上传请求处理器
                this.uploadFileHandler = new UploadFileHandler(this.context);
                //过滤上传请求基本逻辑
                this.uploadFileHandler.filterUpload(ctx, req);
                break;
            //默认服务请求
            case service:
            default:
                handleService(ctx, req);
                break;
        }
    }

    /**
     * 处理http服务请求
     *
     * @param ctx
     * @param req 请求
     */
    private void handleService(ChannelHandlerContext ctx, HttpRequest req) {
        //请求类型
        String command = req.method().toString().toLowerCase();
        //根据请求类型分发
        switch (command) {
            //支持的请求类型
            case "get":
            case "post":
            case "put":
            case "delete":
                //根据path和params处理业务并返回结果
                Object result = handleServiceFactory(req);
                //组装、响应并返回
                ResponseAndEncoderHandler.use().sendObject(ctx, HttpResponseStatus.OK, result);
                break;
            //默认
            case "options":
            default:
                //当做预检请求处理
                ResponseAndEncoderHandler.use().sendOption(ctx);
                break;
        }
    }

    /**
     * 根据请求路径获得服务和方法名
     *
     * @param path eg:/Organize/login
     * @return ["Organize","login"]
     */
    private static List<String> getServiceAndMethod(String path) {
        //初始化
        List<String> result = new ArrayList<>();
        //拆分
        List<String> piecewise = Arrays.asList(path.split("/"));
        //分别组装服务名和方法名
        if (CollectionUtils.isNotEmpty(piecewise) && piecewise.size() > 2) {
            //获取并组装服务名
            result.add(piecewise.get(1));
            //获取第二级
            String second = piecewise.get(2);
            //如果存在?
            if (second.contains("?")) {
                //截取到?之前
                second = second.substring(0, second.lastIndexOf("?"));
            }
            //组装第二级
            result.add(second);
        }
        //返回
        return result;
    }

    /**
     * 通道，请求过来从这里分类
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //获取上下文
        context = ctx.channel().attr(Const.AttrContext).get();
        //获取失败，返回
        if (context == null) {
            return;
        }
        //按请求类型处理
        try {
            //如果是http
            if (msg instanceof HttpRequest) {
                //处理service和upload请求
                handleHttpRequest(ctx, (HttpRequest) msg);
            } else if (msg instanceof HttpContent && uploadFileHandler != null) {
                //处理接受来的upload的form-data内容
                uploadFileHandler.handleHttpFormDataContent(ctx, (HttpContent) msg);
            } else {
                //响应
                ResponseAndEncoderHandler.use().sendFailAndMessage(ctx, HttpResponseStatus.OK, "失败的请求.");
            }
        } catch (Exception e) {
            logger.error("channelRead", e);
        } finally {
            //释放请求
            ReferenceCountUtil.safeRelease(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //判空
        if (uploadFileHandler != null) {
            //清除请求解码
            uploadFileHandler.clearDecoder();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //当连接断开的时候 关闭未关闭的文件流
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                //输入日志
                logger.error("静态资源文件流关闭异常:" + cause);
            }
        }
        ctx.close();
    }

}
