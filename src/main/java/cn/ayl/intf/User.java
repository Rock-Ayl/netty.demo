package cn.ayl.intf;

import cn.ayl.common.annotation.Method;
import cn.ayl.common.annotation.Param;
import cn.ayl.common.annotation.Service;
import cn.ayl.common.json.JsonObject;

/**
 * created by Rock-Ayl 2020/1/3
 * 用户接口
 */
@Service(desc = "用户")
public interface User extends IMicroService {

    @Method(desc = "登录")
    JsonObject login(
            @Param("账户") String key,
            @Param("密码") String password
    );

}
