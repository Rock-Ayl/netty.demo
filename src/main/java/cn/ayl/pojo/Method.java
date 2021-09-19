package cn.ayl.pojo;

import cn.ayl.common.enumeration.ClassType;
import cn.ayl.common.enumeration.RequestMethod;
import cn.ayl.common.enumeration.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求方法的实体
 */
public class Method {

    protected static Logger logger = LoggerFactory.getLogger(Method.class);

    //名称
    public String name;
    //注水
    public String desc;
    //请求方法
    public RequestMethod command;
    //是否需要授权
    public boolean auth;
    //上下文类型
    public ContentType contentType;
    //返回值类型: 一般为Class_ 因为JsonObject就是对象
    public transient ClassType resultType;
    public transient java.lang.reflect.Method method;
    //参数组
    public LinkedHashMap<String, Param> paramMap = new LinkedHashMap();
    public List<String> paramList = new ArrayList<>();
    public List<Class<?>> paramEntryList = new ArrayList<>();

    public Method(cn.ayl.common.annotation.Method method, String name) {
        this.command = method.command();
        this.contentType = method.contentType();
        this.desc = method.desc();
        this.auth = method.auth();
        this.name = name;
    }

    //解析方法实体中的参数
    public void parseParams(java.lang.reflect.Method method, String className) {
        //方法
        this.method = method;
        //确认返回值类型
        this.resultType = ClassType.parseType(this.method.getReturnType());
        //获取参数组
        Parameter[] paramNames = method.getParameters();
        //获取参数类型
        Class<?>[] paramTypes = method.getParameterTypes();
        //获取参数对应的注解
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        //循环参数类型
        for (int i = 0; i < paramTypes.length; i++) {
            //获取参数类型
            Class<?> cls = paramTypes[i];
            if (paramAnnotations[i].length <= 0) {
                logger.error("class[{}] method[{}] all methodMap must have annotation", className, name);
                System.exit(-1);
            }
            //获取注解
            cn.ayl.common.annotation.Param paramAnnotation = (cn.ayl.common.annotation.Param) paramAnnotations[i][0];
            if (!paramNames[i].isNamePresent()) {
                logger.info("java compile[{}] require -parameters,please add '-parameters' in [preferences]->[Build.JavaCompiler]->[Additional Paramaters]", className);
                System.exit(-1);
            }
            //参数名
            String paramName = paramNames[i].getName();
            //参数注释
            String paramDesc = paramAnnotation.value();
            //参数类型
            ClassType type = ClassType.parseType(cls);
            //创建实体
            Param param = new Param(cls, type, paramName, paramDesc, paramAnnotation.optional());
            //组装
            paramMap.put(param.name, param);
            paramList.add(param.name);
            paramEntryList.add(param.clazz);
        }
    }

}
