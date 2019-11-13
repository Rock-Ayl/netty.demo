package cn.ayl.socket.handler;

import cn.ayl.config.Const;
import cn.ayl.entry.MethodEntry;
import cn.ayl.entry.ParamEntry;
import cn.ayl.entry.RegistryEntry;
import cn.ayl.entry.ServiceEntry;
import cn.ayl.util.ScanClassUtil;
import cn.ayl.util.json.JsonObject;
import cn.ayl.util.json.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

import static cn.ayl.config.Const.Json_No_Service;
import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * created by Rock-Ayl on 2019-11-7
 * Http请求和WebSocket请求的处理程序
 */
public class HttpAndWebSocketHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(HttpAndWebSocketHandler.class);

    private WebSocketServerHandshaker webSocketServerHandshaker;

    /**
     * 通道，请求过来从这里分类
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //处理Http请求和WebSocket请求的分别处理
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof HttpContent) {
            //todo handleHttpContent
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 每个channel都有一个唯一的id值
     * asLongText方法是channel的id的全名
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //todo 连接打开时
        logger.info(ctx.channel().localAddress().toString() + " ,handlerAdded！, channelId=" + ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //todo 连接关闭时
        logger.info(ctx.channel().localAddress().toString() + " ,handlerRemoved！, channelId=" + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //todo 出现异常
        logger.error("Client:" + ctx.channel().remoteAddress() + " ,error", cause.getMessage());
        ctx.close();
    }

    // 处理Websocket的代码
    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 文本消息，不支持二进制消息
        if (frame instanceof TextWebSocketFrame) {
            //请求text
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("收到信息:" + request);
            //返回
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonObject.Success().append("req", request).toString()));
        }
    }

    /**
     * 根据path和params处理业务并返回
     *
     * @param path   eg:  /Organize/login
     * @param params eg:  user:root pwd:123456
     * @return
     */
    private Object handleServiceFactory(String path, Map<String, Object> params) {
        //根据请求路径获得服务和方法名
        List<String> serviceAndMethod = getServiceAndMethod(path);
        if (serviceAndMethod.size() < 2) {
            return Json_No_Service;
        }
        //获取服务
        ServiceEntry serviceEntry = RegistryEntry.serviceMap.get(serviceAndMethod.get(0));
        //如果服务存在
        if (serviceEntry == null) {
            return Json_No_Service;
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
        Class serviceClass = ScanClassUtil.findImplClass(serviceEntry.interFaceClass);
        //是否存在实现
        if (serviceClass == null) {
            return Const.Json_No_Impl;
        }
        //实现类
        Object service;
        Object result;
        try {
            //调用构造函数
            Constructor noArgConstructor = serviceClass.getDeclaredConstructor();
            //构建
            service = noArgConstructor.newInstance();
            //组装参数和参数类型
            Object[] valueArr = new Object[paramList.size()];
            Class<?>[] valueTypeArr = new Class[paramList.size()];
            for (int i = 0; i < paramList.size(); i++) {
                //组装参数
                valueArr[i] = params.get(paramList.get(i));
                //组装类型
                valueTypeArr[i] = methodEntry.paramMap.get(paramList.get(i)).clazz;
            }
            //定位服务的方法
            Method method = serviceClass.getMethod("login", valueTypeArr);
            //加入参数并执行
            Object resultObject = method.invoke(service, valueArr);
            //获取返回值
            result = resultObject;
        } catch (Exception e) {
            logger.error("请求构建函数失败, error: [{}]", e);
            return Const.Json_Find_Exception;
        }
        return result;
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //todo http请求内容分类进行细化,目前设定为全部为服务请求(可以存在页面,资源,上传等等)
        handleService(ctx, req);
    }

    /**
     * 处理http服务请求
     *
     * @param ctx
     * @param req
     */
    private void handleService(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse response;
        Object result;
        //获得请求path
        String path = getPath(req);
        //根据请求类型处理请求 get post ...
        if (req.method() == HttpMethod.GET) {
            //获取请求参数
            Map<String, Object> params = getGetParamsFromChannel(req);
            //业务
            result = handleServiceFactory(path, params);
            response = responseOKAndJson(HttpResponseStatus.OK, result);
        } else if (req.method() == HttpMethod.POST) {
            //获取请求参数
            Map<String, Object> params = getPostParamsFromChannel(req);
            //处理业务
            result = handleServiceFactory(path, params);
            response = responseOKAndJson(HttpResponseStatus.OK, result);
        } else {
            //todo 处理其他类型的请求
            response = responseOKAndJson(HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
        }
        // 发送响应并关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 根据请求路径获得服务和方法名
     *
     * @param path
     * @return
     */
    private List<String> getServiceAndMethod(String path) {
        List<String> result = new ArrayList<>();
        //服务名
        String serviceName = null;
        //方法名
        String methodName = null;
        //去掉第一个/
        path = path.substring(1);
        //拆分
        String[] piecewise = path.split("/");
        //分别组装服务名和方法名
        if (piecewise.length >= 2) {
            for (String info : piecewise) {
                if (serviceName == null) {
                    serviceName = info;
                } else {
                    if (methodName == null) {
                        methodName = info;
                        break;
                    }
                }
            }
        }
        //组装
        result.add(serviceName);
        result.add(methodName);
        return result;
    }

    /**
     * Http获取请求Path
     *
     * @param req
     * @return
     */
    private String getPath(FullHttpRequest req) {
        String path = null;
        try {
            path = new URI(req.getUri()).getPath();
        } catch (Exception e) {
            logger.error("接口解析错误.");
        } finally {
            return path;
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
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        } else {
            return null;
        }

    }

    /**
     * Http获取POST方式传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {
        //参数组
        Map<String, Object> params;
        //如果请求为POST
        if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get("Content-Type").trim();
            if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(fullHttpRequest);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJSONParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
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
     * Http解析from表单数据（Content-Type = x-www-form-urlencoded）
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<>();
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        return params;
    }

    /**
     * Http解析json数据（Content-Type = application/json）
     *
     * @param fullHttpRequest
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, Object> getJSONParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<>();
        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");
        JsonObject jsonParams = JsonUtil.parse(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get((String) key));
        }
        return params;
    }

    /**
     * Http响应OK并返回Json
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    private FullHttpResponse responseOKAndJson(HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }

    /**
     * Http响应OK并返回文本
     *
     * @param status 状态
     * @param result 返回值
     * @return
     */
    private FullHttpResponse responseOKAndText(HttpResponseStatus status, Object result) {
        ByteBuf content = copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return response;
    }
}
