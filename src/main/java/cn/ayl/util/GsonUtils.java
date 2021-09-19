package cn.ayl.util;

import com.google.gson.*;

/**
 * 谷歌Gson,工具类,解析神器
 * create by Rock-Ayl 2019-6-13
 */
public class GsonUtils {

    //创建个静态的工具
    private static final Gson Gson = new GsonBuilder().create();
    private static final JsonParser Parser = new JsonParser();

    /**
     * 解析object为Json结构的String
     *
     * @param o 可以转化为JsonString的对象
     * @return
     */
    public static String toJsonString(Object o) {
        return Gson.toJson(o);
    }

    /**
     * 解析String为Gson
     *
     * @param content 可以转化为Gson的String
     * @return
     */
    public static JsonObject parse(String content) {
        return (JsonObject) Parser.parse(content);
    }

    /**
     * 强转对象为任意类型
     *
     * @param clz 类型
     * @param o   被转换对象
     * @param <T>
     * @return
     */
    public static <T> T parseObject(Object o, Class<T> clz) {
        if (null == o) {
            return null;
        }
        return Gson.fromJson(toJsonString(o), clz);
    }

}
