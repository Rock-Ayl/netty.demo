package cn.ayl.entry;

import cn.ayl.config.Const;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求参数的实体
 */
public class ParamEntry {

    public final Class<?> clazz;
    public final String name;
    public final String desc;
    public transient final Const.ClassType type;
    public final boolean optional;

    public ParamEntry(final Class<?> clazz, final Const.ClassType type, final String name, final String desc, boolean optional) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.optional = optional;
    }

}
