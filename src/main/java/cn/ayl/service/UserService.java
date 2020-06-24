package cn.ayl.service;

import cn.ayl.common.user.UserCommons;
import cn.ayl.common.db.redis.Redis;
import cn.ayl.common.json.JsonObject;
import cn.ayl.config.Const;
import cn.ayl.intf.User;
import cn.ayl.socket.rpc.Context;
import cn.ayl.util.IdUtils;
import cn.ayl.util.PatternUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created By Rock-Ayl on 2020-05-13
 * 用户接口的实现类
 */
public class UserService extends Context implements User {

    @Override
    public JsonObject readUserList(String keyword, Integer pageIndex, Integer pageSize) {
        //验证权限为root
        if (!UserCommons.isRoot(this.user.userId)) {
            return Const.Json_No_Permission;
        }
        //验证关键词
        if (StringUtils.isEmpty(keyword)) {
            //缺省
            keyword = "";
        } else if (!PatternUtils.isUserName(keyword)) {
            return Const.Json_Not_Keyword;
        }
        //查询并返回
        return UserCommons.readUserList(keyword, pageIndex, pageSize);
    }

    @Override
    public JsonObject login(String account, String password) {
        //如果key不是手机号
        if (!PatternUtils.isMobile(account)) {
            return Const.Json_Not_Mobile;
        }
        //如果key不是密码
        if (!PatternUtils.isUserName(password)) {
            return Const.Json_Not_Password;
        }
        //获取用户信息
        JsonObject userInfo = UserCommons.readUserInfo(account, password);
        //判空
        if (userInfo == null) {
            return Const.Json_No_User;
        }
        //生成用户cookieId
        String cookieId = IdUtils.newId();
        //组装至用户信息
        userInfo.append("cookieId", cookieId);
        //删除密码
        userInfo.remove("password");
        //将用户登录缓存写入redis中
        Redis.user.set(cookieId, userInfo.toString());
        //返回用户数据
        return JsonObject.Success().append(Const.Data, userInfo);
    }

}
