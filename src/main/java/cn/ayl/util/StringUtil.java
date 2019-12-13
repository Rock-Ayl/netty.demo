package cn.ayl.util;

import cn.ayl.util.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

/**
 * String 工具类
 */
public class StringUtil {

    /**
     * 获取一个唯一性的长id
     *
     * @return
     */
    public static String newId() {
        ObjectId id = new ObjectId();
        return id.toString();
    }

    /**
     * 是否为空
     *
     * @param v
     * @return
     */
    public static boolean isEmpty(String v) {
        if (StringUtils.isEmpty(v)) {
            return true;
        }
        if (v.equalsIgnoreCase("null") || v.equalsIgnoreCase("undefined")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为Json
     *
     * @param content
     * @return
     */
    public static boolean isJson(String content) {
        try {
            JsonUtil.parse(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
