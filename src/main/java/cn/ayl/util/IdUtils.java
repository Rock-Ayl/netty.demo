package cn.ayl.util;

import org.bson.types.ObjectId;

/**
 * create by Rock-Ayl 2020-5-10
 * id 工具类
 */
public class IdUtils {

    /**
     * 获取一个UUID(基于mongo的objectId)
     *
     * @return
     */
    public static String newId() {
        return new ObjectId().toString();
    }

}
