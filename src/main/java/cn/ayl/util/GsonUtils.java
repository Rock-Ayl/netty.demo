package cn.ayl.util;

import com.google.gson.*;

import java.util.Iterator;
import java.util.Map;

/**
 * 谷歌 工具类
 * create by Rock-Ayl 2019-6-13
 */
public class GsonUtils {

    public static Gson gson = new GsonBuilder().create();
    protected static JsonParser parser = new JsonParser();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static JsonObject merge(JsonObject o1, JsonObject o2) {
        JsonObject result = new JsonObject();
        Iterator<Map.Entry<String, JsonElement>> i1 = o1.entrySet().iterator();
        Iterator<Map.Entry<String, JsonElement>> i2 = o2.entrySet().iterator();
        Map.Entry<String, JsonElement> temp;
        while (i1.hasNext()) {
            temp = i1.next();
            result.add(temp.getKey(), temp.getValue());
        }
        while (i2.hasNext()) {
            temp = i2.next();
            result.add(temp.getKey(), temp.getValue());
        }
        return result;
    }

    public static JsonObject parse(String content) {
        return (JsonObject) parser.parse(content);
    }
}
