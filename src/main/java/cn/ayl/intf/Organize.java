package cn.ayl.intf;

import cn.ayl.annotation.Method;
import cn.ayl.annotation.Param;
import cn.ayl.annotation.Service;
import cn.ayl.util.json.JsonObject;

/**
 * created by Rock-Ayl 2019-11-13
 * 用来测试
 */
@Service(desc = "测试相关")
public interface Organize extends IMicroService {

    @Method(desc = "方法相关", auth = true)
    JsonObject login(
            @Param("参数相关") String name,
            @Param("密码") Integer pwd,
            @Param("是否为角色") Boolean isRole
    );

}
