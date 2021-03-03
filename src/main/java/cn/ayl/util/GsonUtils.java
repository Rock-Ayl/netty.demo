package cn.ayl.util;

import com.google.gson.*;

/**
 * 谷歌Gson,工具类,解析神器
 * create by Rock-Ayl 2019-6-13
 */
public class GsonUtils {

    private static Gson Gson = new GsonBuilder().create();
    private static JsonParser Parser = new JsonParser();

    /**
     * 解析object为Json结构的String
     *
     * @param o 可以转化为JsonString的对象
     * @return
     */
    public static String toJson(Object o) {
        return Gson.toJson(o);
    }

    /**
     * 解析String为Json
     *
     * @param content 可以转化为Json的String
     * @return
     */
    public static JsonObject parse(String content) {
        return (JsonObject) Parser.parse(content);
    }

}
