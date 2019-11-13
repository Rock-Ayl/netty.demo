package cn.ayl.entry;

import cn.ayl.annotation.Method;
import cn.ayl.annotation.Service;
import cn.ayl.intf.IMicroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求服务的实体
 */
public class ServiceEntry {

    protected static Logger logger = LoggerFactory.getLogger(ServiceEntry.class);

    //IMicroService的子集
    public Class<? extends IMicroService> interFaceClass;
    //接口名
    public String name = null;
    //注释
    public String desc = null;
    //该服务的方法组
    public List<MethodEntry> methods = new ArrayList();

    public ServiceEntry(Class<? extends IMicroService> interFaceClass) {
        this.interFaceClass = interFaceClass;

    }

    //初始化业务实体
    public Boolean init() {
        //获取注解
        Service ServiceAnnotation = this.interFaceClass.getAnnotation(Service.class);
        //判断是否存在
        if (ServiceAnnotation == null) {
            logger.warn("Class[{}] has not @Service annotation,please define.", this.interFaceClass.getName());
            return false;
        }
        //接口名
        name = this.interFaceClass.getSimpleName();
        //注解注释
        desc = ServiceAnnotation.desc();
        try {
            //获取接口方法[]
            java.lang.reflect.Method[] methods = this.interFaceClass.getMethods();
            if (methods.length > 0) {
                //循环
                for (int i = 0; i < methods.length; i++) {
                    //获取该方法
                    java.lang.reflect.Method method = methods[i];
                    //该方法如果没有@Method注释，忽略
                    if (!method.isAnnotationPresent(Method.class)) {
                        continue;
                    }
                    //获取该方法的@Method注解
                    Method methodAnnotation = method.getAnnotation(Method.class);
                    //方法名
                    String methodName = method.getName();
                    //创建一个方法实体
                    MethodEntry mEntry = new MethodEntry(methodAnnotation, methodName);
                    //解析方法内的参数
                    mEntry.parseParams(method, this.interFaceClass.getName());
                    //方法实体组装
                    this.methods.add(mEntry);
                }
            }
        } catch (Exception e) {
            logger.error("Name[{}]", name, e);
            return false;
        }
        //为方法根据请求类型(get/post)做个排序
        sortMethods();
        return true;
    }

    private void sortMethods() {
        methods.sort((o1, o2) -> o1.compareTo(o2));
    }

}
