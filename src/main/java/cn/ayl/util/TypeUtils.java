package cn.ayl.util;

import com.alibaba.fastjson.JSON;

/**
 * created by Rock-Ayl on 2019-11-14
 * 类型工具类
 */
public class TypeUtils {

    /**
     * 强转类型任意类型
     *
     * @param clz 类型
     * @param o   被转换对象
     * @param <T>
     * @return
     */
    public static <T> T castObject(Class<T> clz, Object o) {
        if (null == o) {
            return null;
        }
        //fastJson的强转,值得信赖
        return JSON.parseObject(JSON.toJSONString(o), clz);
    }
}
