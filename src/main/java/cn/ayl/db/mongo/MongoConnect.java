package cn.ayl.db.mongo;

import cn.ayl.util.json.JsonCodecProvider;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider;
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;
import cn.ayl.config.Const;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.jsr310.Jsr310CodecProvider;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongoConnect {

    protected static int maxConnect = 300;
    protected static int maxWaitThread = 50;
    protected static int maxTimeOut = 130;
    protected static int maxWaitTime = 30;
    public static MongoClientOptions options;
    protected MongoClient client;
    protected String database;

    static {
        MongoClientOptions.Builder optionBuilder = new MongoClientOptions.Builder();
        optionBuilder.connectionsPerHost(Integer.valueOf(maxConnect));
        optionBuilder.threadsAllowedToBlockForConnectionMultiplier(Integer.valueOf(maxWaitThread));
        optionBuilder.connectTimeout(Integer.valueOf(maxTimeOut) * 1000);
        optionBuilder.maxWaitTime(Integer.valueOf(maxWaitTime) * 1000);
        optionBuilder.codecRegistry(fromProviders(asList(new ValueCodecProvider(),
                new BsonValueCodecProvider(),
                new DBRefCodecProvider(),
                new DBObjectCodecProvider(),
                new JsonCodecProvider(new DocumentToDBRefTransformer()),
                new IterableCodecProvider(new DocumentToDBRefTransformer()),
                new MapCodecProvider(new DocumentToDBRefTransformer()),
                new GeoJsonCodecProvider(),
                new GridFSFileCodecProvider(),
                new Jsr310CodecProvider(),
                new JsonCodecProvider())));
        options = optionBuilder.build();
    }

    public MongoConnect(MongoClient client, String database) {
        this.client = client;
        this.database = database;
    }

    public static MongoConnect by(String database) {
        try {

            MongoClient client = new MongoClient(Const.MongoHost, options);
            return new MongoConnect(client, database);
        } catch (Exception e) {
            System.out.println("错误.");
        }
        return null;
    }

    public MongoDatabase getDataBase() {
        return client.getDatabase(database);
    }

}
