package cn.ayl.pojo;

import cn.ayl.common.enumeration.ClassType;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求参数的实体
 */
public class ParamEntry {

    //类
    public final Class<?> clazz;
    //类的类型
    public transient final ClassType type;
    //名称
    public final String name;
    //注释
    public final String desc;
    //是否必须
    public final boolean optional;

    public ParamEntry(final Class<?> clazz, final ClassType type, final String name, final String desc, boolean optional) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.optional = optional;
    }

}
