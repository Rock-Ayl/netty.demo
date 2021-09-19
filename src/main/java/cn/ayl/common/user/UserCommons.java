package cn.ayl.common.user;

import cn.ayl.common.String.StringCommons;
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
     * @param account  账号
     * @param password 密码
     * @return
     */
    public static JsonObject readUserInfo(String account, String password) {
        //查询并返回,不存在会返回null
        return SqlTable.use().queryObject("SELECT * FROM `user` WHERE mobile = ? AND `password` = ?", new Object[]{account, password});
    }

    /**
     * 读取用户列表
     *
     * @param keyword   关键词(检索用户名)
     * @param pageIndex 分页
     * @param pageSize  分页
     * @return
     */
    public static JsonObject readUserList(String keyword, Integer pageIndex, Integer pageSize) {
        //基础查询Sql
        StringBuffer sql = new StringBuffer("SELECT * FROM `user` WHERE userName like '%" + keyword + "%'  ");
        //查询并返回
        return StringCommons.queryItemsAndTotalCount(sql, new Object[]{}, pageIndex, pageSize);
    }

    /**
     * 判断是否为系统管理员
     *
     * @param userId
     * @return
     */
    public static boolean isRoot(long userId) {
        //todo 暂时认为用户id=1是管理员
        if (userId == 1L) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否不为系统管理员
     *
     * @param userId
     * @return
     */
    public static boolean isNotRoot(long userId) {
        return !isRoot(userId);
    }

}
