package cn.ayl.json;

import org.bson.BsonType;

import java.util.ArrayList;
import java.util.List;

public class JsonList<T> {
    private BsonType type = BsonType.NULL;
    public List<T> values;
    public JsonObjects objects = null;

    public JsonList() {
        this.values = new ArrayList();
    }

    public static JsonList create() {
        return new JsonList();
    }

    public boolean isNull() {
        return this.type == BsonType.NULL;
    }

    public boolean isJsonObject() {
        return type == BsonType.DOCUMENT;
    }


    public void init(BsonType type) {
        this.type = type;
        if (type == BsonType.DOCUMENT) {
            objects = new JsonObjects();
        } else {
            values = new ArrayList();
        }
    }

    public void add(Object value) {
        if (type == BsonType.DOCUMENT) {
            this.objects.add((JsonObject) value);
        } else {
            this.values.add((T) value);
        }
    }

    public void addAll(T[] values) {
        for (int i = 0; i < values.length; i++) {
            this.values.add(values[i]);
        }
    }

    public void addAll(JsonList<T> source) {
        if (source.isJsonObject()) {
            this.objects.addAll(source.objects);
        } else {
            this.values.addAll(source.values);
        }
    }


    public void delete(int index) {
        if (type == BsonType.DOCUMENT) {
            this.objects.remove(index);
        } else {
            this.values.remove(index);
        }
    }

    public List<String> strings() {
        return this.values == null ? new ArrayList() : (List<String>) this.values;
    }

    public List<Long> longs() {
        return this.values == null ? new ArrayList() : (List<Long>) this.values;
    }

    public List<Integer> integers() {
        return this.values == null ? new ArrayList() : (List<Integer>) this.values;
    }

    public List<Float> floats() {
        return this.values == null ? new ArrayList() : (List<Float>) this.values;
    }

    public int size() {
        return type == BsonType.DOCUMENT ? this.objects.size() : this.values.size();
    }


    public String toJson() {
        return JsonUtil.toJson(this);
    }

}
