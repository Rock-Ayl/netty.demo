package cn.ayl.entry;

import cn.ayl.config.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求方法的实体
 */
public class MethodEntry {

    public String name;
    public String desc;
    public Const.Command command;
    public boolean auth;
    public Const.ContentType contentType;
    public List<ParamEntry> params = new ArrayList();

    public MethodEntry(cn.ayl.annotation.Method method, String name) {
        this.command = method.command();
        this.contentType = method.contentType();
        this.name = name;
        this.desc = method.desc();
        this.auth = method.auth();
    }

}
