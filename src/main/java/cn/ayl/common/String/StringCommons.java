package cn.ayl.common.String;

import cn.ayl.common.db.jdbc.SqlTable;
import cn.ayl.common.json.JsonObject;
import cn.ayl.config.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created By Rock-Ayl 2020/2/20
 * String业务的共有逻辑
 */
public class StringCommons {

    protected static Logger logger = LoggerFactory.getLogger(StringCommons.class);

    /**
     * 根据Sql查询它的总数和数据
     *
     * @param sql       基础的Sql
     * @param object    对象组,用来补全Sql
     * @param pageIndex 第几页,可以为空
     * @param pageSize  每页几条数据,可以为空
     * @return
     */
    public static JsonObject queryItemsAndTotalCount(StringBuffer sql, Object[] object, Integer pageIndex, Integer pageSize) {
        try {
            //初始化返回值
            JsonObject result = JsonObject.Success();
            //页数范围
            if ((pageIndex != null && pageSize != null) && (pageIndex != 0 && pageSize != 0)) {
                //查询数量
                result.append(Const.TotalCount, SqlTable.use().queryObjects(sql.toString(), object).size());
                //组装sql分页
                sql.append(" LIMIT " + (pageIndex * pageSize - pageSize) + "," + (pageSize) + " ");
            }
            //查询数据并返回
            return result.append(Const.Items, SqlTable.use().queryObjects(sql.toString(), object));
        } catch (Exception e) {
            logger.error("非法查询,exception:[{}]", e);
            return JsonObject.Fail("非法查询");
        }
    }

}
