package cn.ayl.util;

import cn.ayl.config.Const;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;

import java.util.regex.Pattern;

/**
 * create by Rock-Ayl 2019-11-13
 * http请求过来后，解析分配class类型 工具
 */
public class ReflectUtils {

    public static Pattern[] Patterns;

    static {
        Patterns = new Pattern[Const.ClassType.values().length];
        Patterns[Const.ClassType.void_.ordinal()] = null;
        Patterns[Const.ClassType.string_.ordinal()] = Pattern.compile("[\\s\\S]*?");
        Patterns[Const.ClassType.integer_.ordinal()] = Pattern.compile("^-?\\d+$");
        Patterns[Const.ClassType.long_.ordinal()] = Pattern.compile("^-?\\d+$");
        Patterns[Const.ClassType.double_.ordinal()] = Pattern.compile("^(-?\\d+)(\\.\\d+)?$");
        Patterns[Const.ClassType.float_.ordinal()] = Pattern.compile("^(-?\\d+)(\\.\\d+)?$");
        Patterns[Const.ClassType.boolean_.ordinal()] = null;
        Patterns[Const.ClassType.class_.ordinal()] = null;
    }

    //解析类型组
    public static Const.ClassType parseType(Class cls) {
        Const.ClassType type;
        if (cls == String.class) {
            type = Const.ClassType.string_;
        } else if (cls == Integer.class || cls == int.class) {
            type = Const.ClassType.integer_;
        } else if (cls == Long.class || cls == long.class) {
            type = Const.ClassType.long_;
        } else if (cls == Float.class || cls == float.class) {
            type = Const.ClassType.float_;
        } else if (cls == Double.class || cls == double.class) {
            type = Const.ClassType.double_;
        } else if (cls == Boolean.class || cls == boolean.class) {
            type = Const.ClassType.boolean_;
        } else if (cls == Void.class || cls == void.class) {
            type = Const.ClassType.void_;
        } else if (cls == String[].class) {
            type = Const.ClassType.strings_;
        } else if (cls == Integer[].class) {
            type = Const.ClassType.integers_;
        } else if (cls == Long[].class) {
            type = Const.ClassType.longs_;
        } else if (cls == Float[].class) {
            type = Const.ClassType.floats_;
        } else if (cls == Double[].class) {
            type = Const.ClassType.doubles_;
        } else if (cls == Boolean[].class) {
            type = Const.ClassType.booleans_;
        } else if (cls == JsonObject.class) {
            type = Const.ClassType.json_;
        } else if (cls == JsonObjects.class) {
            type = Const.ClassType.jsons_;
        } else {
            type = Const.ClassType.class_;
        }
        return type;
    }


}
