package cn.ayl.util;

import cn.ayl.common.entry.ParamEntry;
import cn.ayl.common.enumeration.ContentType;
import cn.ayl.common.json.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created By Rock-Ayl on 2020-05-18
 * http请求工具类,包括http请求的取参
 */
public class HttpUtils {

    protected static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * Http Get 请求
     *
     * @param url    请求url
     * @param params 请求参数
     * @return
     */
    public static String sendGet(String url, HashMap<String, String> params) {
        //响应
        StringBuffer result = new StringBuffer();
        //缓冲初始化
        BufferedReader in = null;
        try {
            //初始化Get请求的Url
            StringBuffer getUrl = new StringBuffer(url);
            //判空
            if (params != null && params.size() > 0) {
                //代表参数的问号
                getUrl.append("?");
                //循环
                for (Map.Entry<String, String> param : params.entrySet()) {
                    //组装参数
                    getUrl.append(param.getKey() + "=" + param.getValue() + "&");
                }
                //删除最后一个&
                getUrl = getUrl.deleteCharAt(getUrl.length() - 1);
            }
            //创建Url对象
            URL realUrl = new URL(getUrl.toString());
            //打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            //建立实际的连接
            connection.connect();
            //获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            //用BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //当前行
            String line;
            //循环
            while ((line = in.readLine()) != null) {
                //组装
                result.append(line);
            }
        } catch (Exception e) {
            logger.error("发送GET请求出现异常！" + e);
        } finally {
            try {
                //判空
                if (in != null) {
                    //关闭
                    in.close();
                }
            } catch (Exception e2) {
                logger.error("发送GET请求出现异常！" + e2);
            } finally {
                //返回
                return result.toString();
            }
        }
    }

    /**
     * Http Post 请求
     *
     * @param url        请求url
     * @param headers    请求 headers
     * @param bodyEntity post的body,可能是 Json,也可以是 form-data
     * @return
     */
    public static String post(String url, HashMap<String, String> headers, HttpEntity bodyEntity) {
        //创建client
        HttpClient client = new DefaultHttpClient();
        //创建Post请求
        HttpPost post = new HttpPost(url);
        //判空
        if (headers != null && headers.size() > 0) {
            //循环
            for (Map.Entry<String, String> header : headers.entrySet()) {
                //设置headers
                post.setHeader(header.getKey(), header.getValue());
            }
        }
        //body放入post
        post.setEntity(bodyEntity);
        try {
            //执行请求、获得相应
            HttpResponse httpResponse = client.execute(post);
            //获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            //创建缓冲输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
            //创建响应字符
            StringBuilder strBer = new StringBuilder();
            //行
            String line;
            //循环
            while ((line = reader.readLine()) != null) {
                //组装响应字符
                strBer.append(line + "\n");
            }
            //关闭
            inStream.close();
            reader.close();
            //返回响应字符
            return strBer.toString();
        } catch (Exception e) {
            logger.error("post请求异常");
            return null;
        }
    }

    /**
     * 判断一个请求是否为https即拥有SSL
     *
     * @param ctx
     * @return
     */
    public static boolean isHttps(ChannelHandlerContext ctx) {
        if (ctx.pipeline().get(SslHandler.class) != null) {
            return true;
        }
        return false;
    }

    /**
     * 从请求中获取参数
     *
     * @param httpRequest get/post请求
     * @param paramMap    所需参数及参数对象类型,可以为null
     * @return
     */
    public static Map<String, Object> getParams(HttpRequest httpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //初始化
        Map<String, Object> params = null;
        //根据请求类型解析参数
        switch (httpRequest.method().toString().toLowerCase()) {
            //地址栏
            case "get":
                //取get参数的同时过滤不必须的参数
                params = HttpUtils.getParamsFromGet(httpRequest, paramMap);
                break;
            //body
            case "post":
            case "put":
            case "delete":
                //取post参数的同时过滤不必须的参数
                params = HttpUtils.getParamsFromPost(httpRequest, paramMap);
                break;
        }
        //返回
        return params;
    }

    /**
     * 从get请求中获取参数(过滤掉不需要的参数)
     *
     * @param httpRequest get请求
     * @param paramMap    所需的参数组及class类型,可以为null
     * @return
     */
    private static Map<String, Object> getParamsFromGet(HttpRequest httpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //参数组
        Map<String, Object> params = new HashMap<>();
        //如果请求为GET继续
        if (httpRequest.method() == HttpMethod.GET) {
            //获取请求uri
            String uri = httpRequest.uri();
            //将Uri分割成path、参数组
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            //获取参数组
            Map<String, List<String>> paramList = decoder.parameters();
            //循环
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                //判空
                if (paramMap != null) {
                    //如果该参数是我需要的
                    if (paramMap.containsKey(entry.getKey())) {
                        //强转并组装
                        params.put(entry.getKey(), GsonUtils.castObject(paramMap.get(entry.getKey()).clazz, entry.getValue().get(0)));
                    }
                } else {
                    //直接组装
                    params.put(entry.getKey(), entry.getValue().get(0));
                }
            }
        }
        return params;
    }

    /**
     * 从post请求中获取参数(过滤掉不需要的参数)
     *
     * @param httpRequest post请求
     * @param paramMap    所需的参数组及class类型,可以为null
     * @return
     */
    private static Map<String, Object> getParamsFromPost(HttpRequest httpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //初始化餐数据
        Map<String, Object> params = new HashMap<>();
        //如果请求为POST
        if (httpRequest.method() == HttpMethod.POST) {
            //处理POST请求
            ContentType contentType = ContentType.parse(httpRequest.headers().get("Content-Type"));
            //根据内容类型获取参数
            switch (contentType) {
                //application/x-www-form-urlencoded
                case XWWWFormUrlencoded:
                    params = getFormDefaultParamsFromPost(httpRequest, paramMap);
                    break;
                //application/json
                case Json:
                    params = getJsonParamsFromPost(httpRequest, paramMap);
                    break;
            }
            return params;
        }
        return params;
    }

    /**
     * 解析 application/x-www-form-urlencoded 参数(过滤掉不需要的参数)
     *
     * @param httpRequest post请求
     * @param paramMap    所需的参数组及class类型,可以为null
     * @return
     */
    private static Map<String, Object> getFormDefaultParamsFromPost(HttpRequest httpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //返回值初始化
        Map<String, Object> params = new HashMap<>();
        //构建请求解码器
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), httpRequest);
        //获取data
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
        //循环
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                //获取key
                String key = attribute.getName();
                //判空
                if (paramMap != null) {
                    //如果是所需参数
                    if (paramMap.containsKey(key)) {
                        //强转并组装
                        params.put(key, GsonUtils.castObject(paramMap.get(key).clazz, attribute.getValue()));
                    }
                } else {
                    //直接组装
                    params.put(key, attribute.getValue());
                }
            }
        }
        return params;
    }

    /**
     * 解析 application/json 参数(过滤掉不需要的参数)
     *
     * @param httpRequest post请求
     * @param paramMap    所需参数及对应的class类型,可以为null
     * @return
     */
    private static Map<String, Object> getJsonParamsFromPost(HttpRequest httpRequest, LinkedHashMap<String, ParamEntry> paramMap) {
        //初始化参数对象
        Map<String, Object> params = new HashMap<>();
        //强转下请求
        FullHttpRequest fullReq = (FullHttpRequest) httpRequest;
        //获取请求内容
        ByteBuf content = fullReq.content();
        //初始化内容byte[]
        byte[] reqContent = new byte[content.readableBytes()];
        //写入byte[]
        content.readBytes(reqContent);
        //读取成字符并转化为Json
        JsonObject jsonParams;
        try {
            jsonParams = JsonUtils.parse(new String(reqContent, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            //报错
            logger.error("解析byte[]为String出现异常:[{}]", e);
            //返回
            return params;
        }
        //循环
        for (String key : jsonParams.keySet()) {
            //判空
            if (paramMap != null) {
                //如果是所需要的参数
                if (paramMap.containsKey(key)) {
                    //强转并组装
                    params.put(key, GsonUtils.castObject(paramMap.get(key).clazz, jsonParams.get(key)));
                }
            } else {
                //直接组装
                params.put(key, jsonParams.get(key));
            }
        }
        //返回
        return params;
    }

    /**
     * 根据文件名区分Http响应的CONTENT_TYPE
     *
     * @param fileName 文件路径
     * @return
     */
    public static String parseHttpResponseContentType(String fileName) {
        //获取文件后缀
        String fileExt = FilenameUtils.getExtension(fileName);
        //判空
        if (StringUtils.isNotBlank(fileExt)) {
            //小写
            fileExt = fileExt.toLowerCase();
            //分发
            switch (fileExt) {
                case "txt":
                case "html":
                    return "text/html; charset=UTF-8";
                case "text":
                    return "text/plain; charset=UTF-8";
                case "json":
                    return "application/json; charset=UTF-8";
                case "css":
                    return "text/css; charset=UTF-8";
                case "js":
                    return "application/javascript;charset=utf-8";
                case "svg":
                    return "Image/svg+xml; charset=utf-8";
                case "jpeg":
                case "jpg":
                    return "image/jpeg";
                case "csv":
                    return ".csv";
                case "ico":
                    return "image/x-icon";
                case "png":
                    return "image/png";
                case "pdf":
                    return "application/pdf; charset=utf-8";
                case "gif":
                    return "image/gif";
                case "mp3":
                    return "audio/mp3; charset=utf-8";
                case "mp4":
                case "mkv":
                    return "video/mp4; charset=utf-8";
            }
        }
        //缺省
        return "application/octet-stream";
    }

}
