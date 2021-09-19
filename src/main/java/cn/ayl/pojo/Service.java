package cn.ayl.pojo;

import cn.ayl.common.enumeration.RequestMethod;
import cn.ayl.intf.IMicroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求服务的实体
 */
public class Service {

    protected static Logger logger = LoggerFactory.getLogger(Service.class);

    //IMicroService的子集
    public Class<? extends IMicroService> interFaceClass;
    //接口名
    public String name = null;
    //注释
    public String desc = null;
    //该服务的请求类型组
    public LinkedHashMap<String, LinkedHashMap<String, Method>> commandMap = new LinkedHashMap<>();

    public Service(Class<? extends IMicroService> interFaceClass) {
        this.interFaceClass = interFaceClass;
        //循环支持的请求方法类型
        for (RequestMethod value : RequestMethod.values()) {
            //初始化
            commandMap.put(value.toString().toLowerCase(), new LinkedHashMap<>());
        }
    }

    //初始化业务实体
    public Boolean init() {
        //获取注解
        cn.ayl.common.annotation.Service ServiceAnnotation = this.interFaceClass.getAnnotation(cn.ayl.common.annotation.Service.class);
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
                    if (!method.isAnnotationPresent(cn.ayl.common.annotation.Method.class)) {
                        continue;
                    }
                    //获取该方法的@Method注解
                    cn.ayl.common.annotation.Method methodAnnotation = method.getAnnotation(cn.ayl.common.annotation.Method.class);
                    //方法名
                    String methodName = method.getName();
                    //创建一个方法实体
                    Method mEntry = new Method(methodAnnotation, methodName);
                    //解析方法内的参数
                    mEntry.parseParams(method, this.interFaceClass.getName());
                    //获取该方法的请求类型
                    String command = methodAnnotation.command().toString().toLowerCase();
                    //如果支持该请求类型
                    if (this.commandMap.containsKey(command)) {
                        //获取对应请求类型中的方法
                        LinkedHashMap<String, Method> methodMap = this.commandMap.get(command);
                        //如果已经存在
                        if (methodMap.containsKey(methodName)) {
                            logger.error("方法[{}]注册重复,服务出现冲突,停止服务.", methods);
                            //终止
                            System.exit(-1);
                        } else {
                            //方法实体组装
                            methodMap.put(methodName, mEntry);
                            //组装回去
                            this.commandMap.put(command, methodMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Name[{}]", name, e);
            return false;
        }
        return true;
    }

}
