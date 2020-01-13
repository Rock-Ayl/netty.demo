package cn.ayl.common.db.mongo;

import com.mongodb.client.MongoDatabase;
import cn.ayl.config.Const;

public class MongoDB {

    protected MongoDatabase database;

    public MongoDB(MongoDatabase instance) {
        this.database = instance;
    }

    public static MongoDB use() {
        return use(Const.MongoDBName);
    }

    public static MongoDB use(String database) {
        try {
            MongoConnect connect = MongoConnect.by(database);
            return new MongoDB(connect.getDataBase());
        } catch (Exception e) {
            System.out.println("错误.");
        }
        return null;
    }

}




