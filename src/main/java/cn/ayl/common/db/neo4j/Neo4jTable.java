package cn.ayl.common.db.neo4j;

import cn.ayl.common.json.JsonObjects;
import cn.ayl.config.Const;
import cn.ayl.util.GsonUtils;
import cn.ayl.util.JsonUtils;
import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created By Rock-Ayl on 2020-10-10
 * Neo4j-非关系图形数据库
 */
public class Neo4jTable {

    protected static Logger logger = LoggerFactory.getLogger(Neo4jTable.class);

    //Neo4j连接
    private static Driver Driver;

    //初始化
    static {
        try {
            //连接url
            String url = "bolt://" + Const.Neo4jHost + ":" + Const.Neo4jPort;
            //连接
            Driver = GraphDatabase.driver(url, AuthTokens.basic(Const.Neo4jUser, Const.Neo4jPassword));
        } catch (Exception e) {
            logger.error("Neo4j连接初始化失败,可能1:Neo4j服务未启动,可能2:用户身份不被认证.");
        }
    }

    //私有
    private Neo4jTable() {

    }

    /**
     * 默认使用
     *
     * @return
     */
    public static Neo4jTable use() {
        return new Neo4jTable();
    }

    /**
     * 实现CypherSql
     *
     * @param cypherSql cypherSql
     * @return
     */
    public JsonObjects execute(String cypherSql) {
        //获取会话
        try (Session session = Driver.session()) {
            //输出sql
            logger.info("Neo4j Sql Execute:[{}]", cypherSql);
            //查询,并将结果转化为Jsons,返回
            return JsonUtils.parses(GsonUtils.toJson(session.run(cypherSql).list(Record::asMap)));
        } catch (Exception e) {
            logger.error("实现cypherSql[{}]出现异常:", cypherSql, e);
            return null;
        }
    }

}
