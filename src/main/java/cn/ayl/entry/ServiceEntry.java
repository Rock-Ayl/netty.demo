package cn.ayl.entry;

import cn.ayl.intf.IMicroService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求服务的实体
 */
public class ServiceEntry {

    public Class<? extends IMicroService> interFaceClass;
    public String name = null;
    public String desc = null;
    public float version = 0;
    public List<MethodEntry> methods = new ArrayList();

    public ServiceEntry(Class<? extends IMicroService> interFaceClass) {
        this.interFaceClass = interFaceClass;
    }

    public boolean init() {
        return true;
    }

}
