package cn.ayl.socket.encoder;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.AsciiString;

import java.util.HashSet;
import java.util.Set;

/**
 * Created By Rock-Ayl on 2020-06-19
 * 服务器http请求允许的headers参数
 */
public class HttpAccessHeaders {

    //缓存Set
    private static Set<AsciiString> Headers = new HashSet<>();

    static {
        //基础headers
        Headers.add(HttpHeaderNames.CONTENT_TYPE);
        Headers.add(HttpHeaderNames.CONTENT_LENGTH);
        Headers.add(HttpHeaderNames.AUTHORIZATION);
        Headers.add(HttpHeaderNames.ACCEPT);
        Headers.add(HttpHeaderNames.ORIGIN);
        //用来判断是否为ajax
        Headers.add(AsciiString.cached("X-Requested-With"));
        //用户CookieId
        Headers.add(AsciiString.cached("cookieId"));
    }

    /**
     * 获取
     *
     * @return
     */
    public static Set<AsciiString> getAccessHeaders() {
        return Headers;
    }

}
