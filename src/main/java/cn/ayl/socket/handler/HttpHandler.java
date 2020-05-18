package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.common.entry.MethodEntry;
import cn.ayl.common.entry.ParamEntry;
import cn.ayl.common.entry.RegistryEntry;
import cn.ayl.common.entry.ServiceEntry;
import cn.ayl.socket.rpc.Context;
import cn.ayl.util.ScanClassUtils;
import cn.ayl.util.TypeUtils;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
                ServiceEntry serviceEntry = RegistryEntry.serviceMap.get(serviceAndMethod.get(0));
                //如果服务存在
                if (serviceEntry != null) {
                    //获取服务中的方法
                    MethodEntry methodEntry = serviceEntry.methodMap.get(serviceAndMethod.get(1));
                    //如果方法存在
                    if (methodEntry != null) {
                        //返回是否需要验证
                        needAuth = methodEntry.auth;
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
        List<String> serviceAndMethod = getServiceAndMethod(context.uriPath);
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
        ServiceEntry serviceEntry = RegistryEntry.serviceMap.get(serviceName);
        //如果服务存在
        if (serviceEntry == null) {
            return Const.Json_No_Service;
        }
        //获取服务中的方法
        MethodEntry methodEntry = serviceEntry.methodMap.get(serviceAndMethod.get(1));
        //如果方法存在
        if (methodEntry == null) {
            return Const.Json_No_InterFace;
        }
        //获取方法中的参数组
        LinkedHashMap<String, ParamEntry> paramMap = methodEntry.paramMap;
        List<String> paramList = methodEntry.paramList;
        //根据获取请求参数
        Map<String, Object> params = getParamsFromChannelByService(req, paramMap);
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
                //赋予业务类可用的参数
                ((Context) service).ctxUserId = context.ctxUserId;
                ((Context) service).cookieId = context.cookieId;
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
            Method method = serviceClass.getMethod(methodEntry.name, valueTypeArr);
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
     * @throws Exception
     */
    private void handleHttpRequest(final ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        switch (this.context.requestType) {
            //请求静态资源
            case resource:
                //创建一个静态资源处理器
                this.responseHandler = new ResourceHandler();
                //处理请求
                this.responseHandler.handleResource(ctx, req, this.context.uriPath);
                break;
            //请求页面
            case htmlPage:
                //todo
                break;
            //上传请求
            case upload:
                if (this.uploadFileHandler != null) {
                    this.uploadFileHandler.clear();
                }
                //创建一个上传请求处理器
                this.uploadFileHandler = new UploadFileHandler();
                //处理请求
                this.uploadFileHandler.handleRequest(ctx, req);
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
        //根据请求类型处理请求 get post ...
        if (req.method() == HttpMethod.GET || req.method() == HttpMethod.POST) {
            //根据path和params处理业务并返回结果
            Object result = handleServiceFactory(req);
            //组装、响应并返回
            ResponseHandler.sendJson(ctx, HttpResponseStatus.OK, result);
        } else {
            //当做预检请求处理
            ResponseHandler.sendOption(ctx);
        }
    }

    /**
     * 根据请求类型获取参数
     *
     * @param req
     * @return
     */
    private Map<String, Object> getParamsFromChannelByService(HttpRequest req, LinkedHashMap<String, ParamEntry> paramMap) {
        Map<String, Object> params = null;
        //如果是get
        if (req.method() == HttpMethod.GET) {
            //获取get请求的参数
            params = getGetParamsFromChannel(req, paramMap);
        } else if (req.method() == HttpMethod.POST) {
            //获取post请求的参数
            params = getPostParamsFromChannel(req, paramMap);
        }
        return params;
    }

    /**
     * 根据请求路径获得服务和方法名
     *
     * @param path eg:/Organize/login
     * @return
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
            //获取并组装方法名
            result.add(piecewise.get(2));
        }
        //返回
        return result;
    }

    /**
     * Http获取GET方式传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getGetParamsFromChannel(HttpRequest fullHttpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //参数组
        Map<String, Object> params = new HashMap<>();
        //如果请求为GET继续
        if (fullHttpRequest.method() == HttpMethod.GET) {
            // 处理get请求
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                //如果该参数是我需要的
                if (paramMap.containsKey(entry.getKey())) {
                    //强转并组装
                    params.put(entry.getKey(), TypeUtils.castObject(paramMap.get(entry.getKey()).clazz, entry.getValue().get(0)));
                }
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * 根据已知参数 从POST方式的Http请求 获取传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getPostParamsFromChannel(HttpRequest fullHttpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //参数组
        Map<String, Object> params;
        //如果请求为POST
        if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get("Content-Type").trim();
            //从from中获取
            if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(fullHttpRequest, paramMap);
            } else if (strContentType.contains("application/json")) {
                try {
                    //从body中获取
                    params = getJSONParams(fullHttpRequest, paramMap);
                } catch (UnsupportedEncodingException e) {
                    logger.error("从body中获取参数失败");
                    return null;
                }
            } else {
                return null;
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * 根据已知参数组从Http解析from表单数据（Content-Type = x-www-form-urlencoded）
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getFormParams(HttpRequest fullHttpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        Map<String, Object> params = new HashMap<>();
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                String attributeName = attribute.getName();
                //如果是所需参数
                if (paramMap.containsKey(attributeName)) {
                    //强转并组装
                    params.put(attributeName, TypeUtils.castObject(paramMap.get(attributeName).clazz, attribute.getValue()));
                }
            }
        }
        return params;
    }

    /**
     * Http解析json数据（Content-Type = application/json）
     *
     * @param httpRequest
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, Object> getJSONParams(HttpRequest httpRequest, LinkedHashMap<String, ParamEntry> paramMap) throws UnsupportedEncodingException {
        //初始化参数对象
        Map<String, Object> params = new HashMap<>();
        //强转下请求
        FullHttpRequest fullReq = (FullHttpRequest) httpRequest;
        ByteBuf content = fullReq.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");
        JsonObject jsonParams = JsonUtil.parse(strContent);
        for (String key : jsonParams.keySet()) {
            //如果是所需参数
            if (paramMap.containsKey(key)) {
                //强转并组装
                params.put(key, TypeUtils.castObject(paramMap.get(key).clazz, jsonParams.get(key)));
            }
        }
        return params;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("通道不活跃的");
        super.channelInactive(ctx);
        //判空
        if (uploadFileHandler != null) {
            uploadFileHandler.clear();
        }
    }

    /**
     * 通道，请求过来从这里分类
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
                //处理接受来的upload文件块
                uploadFileHandler.handleHttpContent(ctx, (HttpContent) msg);
            } else {
                //响应
                ResponseHandler.sendMessageOfJson(ctx, HttpResponseStatus.OK, "失败的请求.");
            }
        } catch (Exception e) {
            logger.error("channelRead", e);
        } finally {
            //释放请求
            ReferenceCountUtil.safeRelease(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //todo 出现异常
        logger.error("Client:" + ctx.channel().remoteAddress() + " ,error", cause.getMessage());
        ctx.close();
    }

}
