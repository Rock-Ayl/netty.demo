package cn.ayl.common.db.neo4j;

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
        Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "123456"));
        try (Session session = driver.session()) {
            try (Transaction transaction = session.beginTransaction()) {
                transaction.run("create(n:A1{NAME:{NAME},TITLE:{TITLE}})", Values.parameters("NAME", "james", "TITLE", "King"));
                transaction.success();
            }
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("match(a:A1) WHERE a.NAME = {NAME} RETURN a.NAME AS NAME,a.TITLE AS TITLE", Values.parameters("NAME", "james"));
                while (result.hasNext()) {
                    Record record = result.next();
                    System.out.println(String.format("%s %s", record.get("TITLE").asString(), record.get("NAME").asString()));

                }
            }
        }
        driver.close();
    }

}
