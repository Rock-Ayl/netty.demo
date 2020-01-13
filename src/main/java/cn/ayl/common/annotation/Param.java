package cn.ayl.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by Rock-Ayl 2019-11-13
 * 参数注释
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

    //注释
    String value() default "";

    //非必须 false:必须 true:不必须
    boolean optional() default false;

}
