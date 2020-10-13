package cn.ayl.common.db.neo4j;

import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import org.neo4j.driver.v1.*;

/**
 * Created By Rock-Ayl on 2020-10-10
 * Neo4j-非关系图形数据库
 */
public class Neo4jTable {

    /**
     * 测试主方法
     *
     * @param args
     */
    public static void main(String[] args) {
        //创建连接
        try (Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "123456"))) {
            //建立会话
            try (Session session = driver.session()) {
                //新增事务
                try (Transaction transaction = session.beginTransaction()) {
                    //新增逻辑
                    transaction.run("create(n:A1{NAME:{NAME},TITLE:{TITLE}})", Values.parameters("NAME", "james", "TITLE", "King"));
                    //提交
                    transaction.success();
                }
                //查询结果
                JsonObjects items = JsonObjects.VOID();
                //查询事务
                try (Transaction tx = session.beginTransaction()) {
                    //查询逻辑
                    StatementResult result = tx.run("match(a:A1) WHERE a.NAME = {NAME} RETURN a.NAME AS NAME,a.TITLE AS TITLE", Values.parameters("NAME", "james"));
                    //编辑
                    while (result.hasNext()) {
                        //获取当前查询记录
                        Record record = result.next();
                        //初始化记录对象
                        JsonObject data = JsonObject.VOID();
                        //组装
                        data.append("TITLE", record.get("TITLE").asString());
                        data.append("NAME", record.get("NAME").asString());
                        //记录至结果
                        items.add(data);
                    }
                    //输出
                    System.out.println(items.toJson());
                }
            }
        }
    }

}
