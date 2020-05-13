package cn.ayl.service;

import cn.ayl.common.user.UserCommons;
import cn.ayl.common.db.redis.Redis;
import cn.ayl.common.json.JsonObject;
import cn.ayl.config.Const;
import cn.ayl.intf.User;
import cn.ayl.util.IdUtils;
import cn.ayl.util.PatternUtils;

/**
 * Created By Rock-Ayl on 2020-05-13
 * 用户接口的实现类
 */
public class UserService implements User {

    @Override
    public JsonObject login(String key, String password) {
        //如果key不是手机号
        if (!PatternUtils.isMobile(key)) {
            return Const.Json_Not_Mobile;
        }
        //如果key不是密码
        if (!PatternUtils.isUserName(password)) {
            return Const.Json_Not_Password;
        }
        //获取用户信息
        JsonObject userInfo = UserCommons.getUserInfo(key, password);
        //判空
        if (userInfo == null) {
            return Const.Json_No_User;
        }
        //获取用户id
        String userId = userInfo.getString("userId");
        //生成用户cookieId
        String cookieId = IdUtils.newId();
        //组装至用户信息
        userInfo.append("cookieId", cookieId);
        //删除密码
        userInfo.remove("password");
        //写入redis中
        Redis.user.set(userId, userInfo.toString());
        //返回
        return JsonObject.Success().append(Const.Data, userInfo);
    }

}
