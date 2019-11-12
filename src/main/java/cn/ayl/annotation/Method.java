package cn.ayl.annotation;

import cn.ayl.config.Const;

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

    //方法备注
    String desc() default "";

    //请求类型
    Const.Command command() default Const.Command.post;

    //返回值类型
    Const.ContentType contentType() default Const.ContentType.json;

    //是否需要授权
    boolean auth() default false;

}
