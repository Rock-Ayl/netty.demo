package cn.ayl.common.user;

import cn.ayl.common.db.jdbc.SqlTable;
import cn.ayl.common.json.JsonObject;

/**
 * Created By Rock-Ayl on 2020-05-13
 * 用户通用业务逻辑
 */
public class UserCommons {

    /**
     * 根据账号密码获取用户信息
     *
     * @param key      账号
     * @param password 密码
     * @return
     */
    public static JsonObject getUserInfo(String key, String password) {
        return SqlTable.use().queryObject("SELECT * FROM `user` WHERE mobile = ? AND `password` = ?", new Object[]{key, password});
    }

}
