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

    @Method(desc = "获取用户列表", auth = true)
    JsonObject readUserList(
            @Param(value = "关键词", optional = true) String keyword,
            @Param(value = "第几页", optional = true) Integer pageIndex,
            @Param(value = "每页几条数据", optional = true) Integer pageSize
    );

    @Method(desc = "用户登录")
    JsonObject login(
            @Param("账户") String account,
            @Param("密码") String password
    );

}
