package cn.ayl.entry;

import cn.ayl.annotation.Param;
import cn.ayl.config.Const;
import cn.ayl.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;

/**
 * Created by Rock-Ayl on 2019-11-13
 * 承载请求方法的实体
 */
public class MethodEntry {

    protected static Logger logger = LoggerFactory.getLogger(MethodEntry.class);

    //名称
    public String name;
    //注水
    public String desc;
    //请求类型 get or post
    public Const.Command command;
    //是否需要授权
    public boolean auth;
    //上下文类型
    public Const.ContentType contentType;
    //返回值类型: 一般为Class_ 因为JsonObject就是对象
    public transient Const.ClassType resultType;
    public transient Method method;
    //参数组
    public LinkedHashMap<String, ParamEntry> params = new LinkedHashMap();

    public MethodEntry(cn.ayl.annotation.Method method, String name) {
        this.command = method.command();
        this.contentType = method.contentType();
        this.desc = method.desc();
        this.auth = method.auth();
        this.name = name;
    }

    //解析方法实体中的参数
    public void parseParams(Method method, String className) {
        //方法
        this.method = method;
        //确认返回值类型
        this.resultType = ReflectUtil.parseType(this.method.getReturnType());
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
            Param paramAnnotation = (Param) paramAnnotations[i][0];
            if (!paramNames[i].isNamePresent()) {
                logger.info("java compile[{}] require -parameters,please add '-parameters' in [preferences]->[Build.JavaCompiler]->[Additional Paramaters]", className);
                System.exit(-1);
            }
            //参数名
            String paramName = paramNames[i].getName();
            //参数注释
            String paramDesc = paramAnnotation.value();
            //参数类型
            Const.ClassType type = ReflectUtil.parseType(cls);
            //创建实体
            ParamEntry param = new ParamEntry(cls, type, paramName, paramDesc, paramAnnotation.optional());
            //组装
            params.put(param.name, param);
        }
    }

    private int weight() {
        switch (this.command) {
            case post:
                return 0;
            case get:
                return 1;
            default:
                return 0;
        }
    }

    protected int compareTo(MethodEntry source) {
        return this.weight() - source.weight();
    }

}
