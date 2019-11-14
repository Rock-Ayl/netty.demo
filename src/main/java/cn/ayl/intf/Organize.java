package cn.ayl.intf;

import cn.ayl.annotation.Method;
import cn.ayl.annotation.Param;
import cn.ayl.annotation.Service;
import cn.ayl.util.json.JsonObject;

/**
 * created by Rock-Ayl 2019-11-13
 * 用来测试
 */
@Service(desc = "测试相关，所有继承于IMicroService的都是服务")
public interface Organize extends IMicroService {

    @Method(desc = "方法相关，auth逻辑暂时没写，目的是用来做身份验证用的，暂时可写可不写", auth = true)
    JsonObject login(
            @Param("参数相关") String name,
            @Param("密码") Integer pwd,
            @Param(value = "是否为角色,optional=true代表这个参数可以传可以不穿", optional = true) Boolean isRole
    );

    @Method(desc = "获取用户", auth = true)
    JsonObject getUser(
            @Param("用户id") Long userId
    );

}
