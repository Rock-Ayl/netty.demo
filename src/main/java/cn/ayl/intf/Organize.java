package cn.ayl.intf;

import cn.ayl.common.annotation.Method;
import cn.ayl.common.annotation.Param;
import cn.ayl.common.annotation.Service;
import cn.ayl.common.json.JsonObject;

/**
 * created by Rock-Ayl 2019-11-13
 * 用来测试
 */
@Service(desc = "测试相关，所有继承于IMicroService的都是服务")
public interface Organize extends IMicroService {

    @Method(desc = "方法相关")
    JsonObject login(
            @Param("参数相关") String name,
            @Param("密码") Integer pwd,
            @Param(value = "是否为角色,optional=true代表这个参数可以传可以不穿", optional = true) Boolean isRole
    );

    @Method(desc = "获取用户,auth的逻辑默认为false，如果打开它，将会验证用户身份", auth = true)
    JsonObject getUser(
            @Param("用户id") Long userId
    );

}
