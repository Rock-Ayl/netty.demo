package cn.ayl.common.db.mongo;

import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import cn.ayl.config.Const;
import org.apache.commons.lang3.RandomUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * mongoDB核心
 * create by Rock-Ayl 2019-6-12
 */
public class MongoTable {

    public static JsonObject VOID = JsonObject.VOID();

    private MongoCollection<JsonObject> collection;

    public static MongoTable use(String collectName) {
        return use(Const.MongoDBName, collectName);
    }

    private static MongoTable use(String database, String collectName) {
        MongoTable table = null;
        if (table == null) {
            table = new MongoTable(MongoDB.use(database).database, collectName);
        }
        return table;
    }

    public MongoTable(MongoDatabase db, String name) {
        this.collection = db.getCollection(name, JsonObject.class);
    }

    public MongoCollection<JsonObject> collection() {
        return this.collection;
    }


    public void insert(JsonObjects docs) {
        this.collection.insertMany(docs);
    }

    public String insert(JsonObject doc) {
        this.collection.insertOne(doc);
        return doc.getObjectId("_id").toString();
    }

    public int update(Bson query, JsonObject data) {
        UpdateOptions options = new UpdateOptions();
        options.upsert(true);
        UpdateResult wr = this.collection.updateOne(query, data, options);
        return (int) wr.getModifiedCount();
    }

    public int updateById(String id, JsonObject data) {
        UpdateOptions options = new UpdateOptions();
        options.upsert(true);
        UpdateResult wr = this.collection.updateOne(createDocumentById(id), data, options);
        return (int) wr.getModifiedCount();
    }


    public int delete(Bson query) {
        DeleteResult deleteResult = this.collection.deleteMany(query);
        return (int) deleteResult.getDeletedCount();
    }

    public int deleteById(String id) {
        DeleteResult deleteResult = this.collection.deleteOne(new Document("_id", new ObjectId(id)));
        return (int) deleteResult.getDeletedCount();
    }

    public int updateAll(Bson query, JsonObject data) {
        UpdateResult updateResult = this.collection.updateMany(query, data);
        return (int) updateResult.getModifiedCount();
    }

    public void drop() {
        this.collection.drop();
    }

    public JsonObjects find(Bson query) {
        return this.find(query, VOID);
    }

    public JsonObjects find(Bson query, Bson projection) {
        return find(query, projection, VOID, 0, 0);
    }


    public JsonObjects find(Bson query, Bson projection, int limit) {
        return find(query, projection, VOID, 0, limit);
    }

    public JsonObjects find(Bson query, int skip, int limit) {
        return find(query, VOID, VOID, skip, limit);
    }

    public JsonObjects find(Bson query, Bson sort, int skip, int limit) {
        return find(query, VOID, sort, skip, limit);
    }

    public JsonObjects find(Bson query, Bson projection, Bson sort, int skip, int limit) {
        JsonObjects results = JsonObjects.VOID();
        find(query, projection, sort, skip, limit, results);
        return results;
    }

    public int find(Bson query, JsonObjects results) {
        return this.find(query, VOID, VOID, 0, 0, results);
    }

    public int find(Bson query, int skip, int limit, JsonObjects results) {
        return this.find(query, VOID, VOID, skip, limit, results);
    }

    public int find(Bson query, Bson sort, int skip, int limit, JsonObjects results) {
        return this.find(query, VOID, sort, skip, limit, results);
    }

    public int find(Bson query, Bson projection, Bson sort, int skip, int limit, JsonObjects results) {
        int totalCount = (int) this.collection.count(query);
        if (totalCount == 0) {
            return 0;
        }
        FindIterable<JsonObject> iterable = this.collection.find(query);
        iterable.projection(projection);
        if (sort != null) {
            iterable.sort(sort);
        }
        if (limit == 0) {
            limit = totalCount;
        }
        skip = skip < 0 ? 0 : skip;
        iterable.skip(skip);
        iterable.limit(limit);
        for (JsonObject doc : iterable) {
            results.add(doc);
        }
        return totalCount;
    }

    public JsonObject idBy(String id) {
        return findOne(createDocumentById(id), VOID, VOID);
    }

    public JsonObject findOne(Bson query) {
        return findOne(query, VOID, VOID);
    }

    public JsonObject findById(String id) {
        return findOne(createDocumentById(id), VOID, VOID);
    }

    public JsonObject findOne(Bson query, Bson projection) {
        return findOne(query, projection, VOID);
    }

    public JsonObject findOne(Bson query, Bson projection, Bson sort) {
        FindIterable<JsonObject> iterable = this.collection.find(query, JsonObject.class);
        iterable.projection(projection);
        if (sort != null) {
            iterable.sort(sort);
        }
        MongoCursor<JsonObject> cursor = iterable.iterator();
        if (cursor.hasNext()) {
            return cursor.next();
        }
        return null;
    }


    public MongoCollection<JsonObject> records() {
        return this.collection;
    }


    public JsonObjects random(Bson query, Bson projection, int count) {
        FindIterable<JsonObject> iterable = this.collection.find(query, JsonObject.class);
        iterable.projection(projection);
        int totalCount = (int) this.collection.count(query);
        int index = RandomUtils.nextInt(0, totalCount) - count;
        if (index <= 0) {
            index = 0;
        }
        if (index >= totalCount) {
            index = totalCount - count;
        }
        iterable.skip(index);
        JsonObjects result = JsonObjects.VOID();
        MongoCursor<JsonObject> cursor = iterable.iterator();
        while (cursor.hasNext() && count >= 0) {
            result.add(cursor.next());
            count--;
        }
        return result;
    }

    public boolean exist(Bson query) {
        return this.collection.count(query) > 0;
    }

    public boolean exist(ObjectId id) {
        return this.exist(JsonObject.Create("_id", id));
    }

    public boolean exist(String id) {
        return this.exist(createDocumentById(id));
    }

    public int count() {
        return (int) this.collection.count();
    }

    public int countBy(Bson query) {
        return (int) this.collection.count(query);
    }

    public AggregateIterable<JsonObject> aggregate(List<Bson> pipeline) {
        return this.collection.aggregate(pipeline);
    }

    public String createIndex(String name, Bson fields) {
        MongoCursor<JsonObject> docs = this.collection.listIndexes(JsonObject.class).iterator();
        while (docs.hasNext()) {
            JsonObject doc = docs.next();
            if (doc.getString("name").equals(name)) {
                this.collection.dropIndex(name);
                break;
            }
        }
        IndexOptions options = new IndexOptions();
        options.background(true);
        options.name(name);
        return this.collection.createIndex(fields, options);
    }

    private Document createDocumentById(String id) {
        return new Document("_id", createObjectId(id));
    }

    public ObjectId createObjectId(String id) {
        return new ObjectId(id);
    }

}