package cn.ayl.common.enumeration;

import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;

/**
 * Java对象类型
 */
public enum ClassType {

    void_, string_, integer_, long_, double_, float_, boolean_,
    strings_, integers_, longs_, doubles_, floats_, booleans_,
    enum_, json_, jsons_, class_;

    /**
     * 解析类型
     *
     * @param cls
     * @return
     */
    public static ClassType parseType(Class cls) {
        ClassType type;
        if (cls == String.class) {
            type = ClassType.string_;
        } else if (cls == Integer.class || cls == int.class) {
            type = ClassType.integer_;
        } else if (cls == Long.class || cls == long.class) {
            type = ClassType.long_;
        } else if (cls == Float.class || cls == float.class) {
            type = ClassType.float_;
        } else if (cls == Double.class || cls == double.class) {
            type = ClassType.double_;
        } else if (cls == Boolean.class || cls == boolean.class) {
            type = ClassType.boolean_;
        } else if (cls == Void.class || cls == void.class) {
            type = ClassType.void_;
        } else if (cls == String[].class) {
            type = ClassType.strings_;
        } else if (cls == Integer[].class) {
            type = ClassType.integers_;
        } else if (cls == Long[].class) {
            type = ClassType.longs_;
        } else if (cls == Float[].class) {
            type = ClassType.floats_;
        } else if (cls == Double[].class) {
            type = ClassType.doubles_;
        } else if (cls == Boolean[].class) {
            type = ClassType.booleans_;
        } else if (cls == JsonObject.class) {
            type = ClassType.json_;
        } else if (cls == JsonObjects.class) {
            type = ClassType.jsons_;
        } else if (cls == Enum.class) {
            type = ClassType.enum_;
        } else {
            type = ClassType.class_;
        }
        return type;
    }

}
