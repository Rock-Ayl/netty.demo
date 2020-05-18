package cn.ayl.common.annotation;

import cn.ayl.common.enumeration.RequestMethod;
import cn.ayl.common.enumeration.ContentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by Rock-Ayl 2019-11-13
 * 类方法注释
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Method {

    //注释
    String desc() default "";

    //请求方法
    RequestMethod command() default RequestMethod.post;

    //内容类型
    ContentType contentType() default ContentType.Json;

    //是否需要授权
    boolean auth() default false;

    //路径
    String path() default "";
}
