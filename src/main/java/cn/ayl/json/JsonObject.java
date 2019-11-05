package cn.ayl.json;

import cn.ayl.util.GsonUtil;
import cn.ayl.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class JsonObject extends Document {

    public static JsonObject VOID() {
        return new JsonObject();
    }

    public static JsonObject NotFound() {
        return VOID().fail("数据不存在");
    }

    public static JsonObject Create(final String key, final Object value) {
        JsonObject result = new JsonObject();
        return result.append(key, value);
    }

    public static JsonObject NotLogin() {
        return VOID().notLogin();
    }

    public static JsonObject Fail(String message) {
        return VOID().fail(message);
    }

    public boolean isSuccess() {
        return this.getBoolean("isSuccess", true);
    }

    public JsonObject notExist() {
        this.append("message", "记录不存在");
        return this;
    }

    public JsonObject notLogin() {
        this.fail("用户未登录");
        return this;
    }


    public static JsonObject Success() {
        return VOID().success();
    }

    public static JsonObject Success(String message) {
        return VOID().success(message);
    }

    public JsonObject fail(String message) {
        return fail().append("message", message);
    }

    public JsonObject fail() {
        this.append("isSuccess", false);
        return this;
    }

    public JsonObject fail(Exception e) {
        return fail().append("message", e.getMessage());
    }

    public JsonObject error(String errCode) {
        this.append("err", errCode);
        return this;
    }


    public JsonObject success(final String key, final Object value) {
        JsonObject result = new JsonObject();
        result.success();
        return result.append(key, value);
    }

    public JsonObject success(String message) {
        return success("message", message);
    }

    public JsonObject success() {
        this.append("isSuccess", true);
        return this;
    }

    public JsonObjects list(String keyFieldName) {
        JsonObjects result = new JsonObjects();
        for (Iterator<Map.Entry<String, Object>> i = this.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Object> entry = i.next();
            JsonObject item = (JsonObject) entry.getValue();
            if (!StringUtil.isEmpty(keyFieldName)) {
                item.append(keyFieldName, entry.getKey());
            }
            result.add(item);
        }
        return result;
    }

    public JsonObjects list() {
        return this.list(null);
    }


    public HashSet<String> hashSet(String fieldName) {
        List<String> items = JsonUtil.parseStrings(this.getString(fieldName));
        HashSet<String> set = new HashSet();
        for (int i = 0; items != null && i < items.size(); i++) {
            set.add(items.get(i));
        }
        return set;
    }

    public boolean isEmpty(String fieldName) {
        String v = this.getString(fieldName);
        return StringUtil.isEmpty(v);
    }

    public JsonObject checkEmpty(String[] fields) {
        JsonObject oResult = new JsonObject();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (this.containsKey(field) == false) {
                continue;
            }
            oResult.put(field, "");
        }
        return oResult;
    }


    public void checkEmpty(String fieldName, Object defaultValue) {
        String fieldValue = this.getString(fieldName);
        if (StringUtils.isEmpty(fieldValue)) {
            this.append(fieldName, defaultValue);
        }
    }

    public JsonObject append(final String key, final Object value) {
        super.append(key, value);
        return this;
    }

    public JsonObject normalizeId() {
        if (!this.containsKey("_id")) return this;
        ObjectId id = this.getObjectId("_id");
        this.remove("_id");
        this.append("id", id.toString());
        return this;
    }

    public List<String> getStrings(final String key) {
        Object result = this.get(key);
        if (result instanceof String) {
            return JsonUtil.parseStrings(String.valueOf(result));
        } else {
            return (List<String>) result;
        }
    }

    public String[] getStringArray(final String key) {
        Object result = this.get(key);
        List<String> values;
        if (result instanceof String) {
            values = JsonUtil.parseStrings(String.valueOf(result));
            return arrayToStrings(values);
        } else if (result instanceof List) {
            values = (List<String>) this.get(key);
            return arrayToStrings(values);
        } else {
            return (String[]) result;
        }
    }

    public JsonObject[] getObjectArray(final String key) {
        Object result = this.get(key);
        List<JsonObject> values;
        if (result instanceof String) {
            values = JsonUtil.parses(String.valueOf(result));
            return arrayToObjects(values);
        } else if (result instanceof List) {
            values = (List<JsonObject>) this.get(key);
            return arrayToObjects(values);
        } else {
            return (JsonObject[]) result;
        }
    }

    public List<Integer> getIntegers(final String key) {
        Object result = this.get(key);
        List<Integer> values;
        if (result instanceof String) {
            values = JsonUtil.parseIntegers(String.valueOf(result));
            return values;
        } else {
            return (List<Integer>) result;
        }
    }

    public List<Long> getLongs(final String key) {
        Object result = this.get(key);
        List<Long> values;
        if (result instanceof String) {
            values = JsonUtil.parseLongs(String.valueOf(result));
            return values;
        } else {
            return (List<Long>) result;
        }
    }

    public float getFloat(String key) {
        return (float) this.get(key);
    }

    public List<Float> getFloats(final String key) {
        Object result = this.get(key);
        List<Float> values;
        if (result instanceof String) {
            values = JsonUtil.parseFloats(String.valueOf(result));
            return values;
        } else {
            return (List<Float>) result;
        }
    }

    public JsonObject getObject(final String key) {
        Object v = this.get(key);
        if (v instanceof String) {
            return JsonUtil.parse((String) v);
        } else {
            return (JsonObject) v;
        }
    }

    public JsonObject getObject(String fieldName, int index) {
        Object value = this.get(fieldName);
        if (value instanceof List) {
            List list = ((List) value);
            if (list == null || list.size() == 0) return null;
            return (JsonObject) list.get(index);
        }
        return null;
    }

    public JsonObjects getObjects(final String key) {
        Object result = this.get(key);
        if (result == null) {
            return null;
        } else if (result instanceof String) {
            return JsonUtil.parses(String.valueOf(result));
        } else if (result instanceof JsonObjects) {
            return (JsonObjects) result;
        } else if (result instanceof ArrayList && ((ArrayList) result).size() == 0) {
            result = JsonObjects.VOID();
            this.append(key, result);
            return (JsonObjects) result;
        } else {
            throw new RuntimeException("[" + key + "] is " + result.getClass().getName() + ",not JsonObjects");
        }
    }

    public String getString(String[] fields) {
        for (int i = 0; i < fields.length; i++) {
            String value = getString(fields[i]);
            if (StringUtils.isEmpty(value)) continue;
            return value;
        }
        return "";
    }

    private String valueToString(Object v) {
        if (v instanceof Object[] || v instanceof List) {
            return GsonUtil.toJson(v);
        }
        return v.toString();
    }

    public String getString(final String key) {
        Object value = this.get(key);
        if (value == null) {
            return null;
        }
        return valueToString(value);
    }

    public String getString(final String key, String defaultValue) {
        Object value = this.get(key);
        if (value == null) {
            return defaultValue;
        }
        return valueToString(value);
    }

    public int getInt(final String key) {
        return this.getInt(key, 0);
    }

    public int getInt(final String key, int defaultValue) {
        Object value = this.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else {
            return (int) value;
        }
    }

    public long getLong(String key) {
        if (this.containsKey(key)) {
            String value = String.valueOf(this.get(key));
            return Long.parseLong(value);
        } else {
            return 0L;
        }
    }

    public Long getLong(final String key, long defaultValue) {
        Long value = this.getLong(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }


    public Object selectXPath(String path) {
        String[] paths = path.split("\\.");
        JsonObject child = this;
        for (int i = 0; i < paths.length - 1; i++) {
            Object o = child.get(paths[i]);
            if (o == null || (o instanceof JsonObject) == false) {
                return null;
            }
            child = (JsonObject) o;
        }
        return child.get(paths[paths.length - 1]);
    }

    public JsonObject selectObject(String path) {
        return (JsonObject) this.selectXPath(path);
    }

    public String selectString(String path) {
        return (String) this.selectXPath(path);
    }

    public float selectFloat(String path) {
        String o = this.selectString(path);
        if (o == null) return 0;
        return Float.parseFloat(o);
    }

    public JsonObject clone() {
        return JsonUtil.parse(this.toJson());
    }

    public JsonObject select() {
        return select(new String[0]);
    }

    public JsonObject select(String... fields) {
        JsonObject oResult = new JsonObject();
        if (fields == null || fields.length == 0) {
            oResult.putAll(this);
            return oResult;
        }
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (this.containsKey(field) == false) {
                continue;
            }
            oResult.put(field, this.get(field));
        }
        return oResult;
    }

    public void removeEmpty() {
        ArrayList<String> fields = new ArrayList();
        for (Iterator<Map.Entry<String, Object>> i = this.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Object> entry = i.next();
            if ((entry.getValue() instanceof String) &&
                    StringUtils.isEmpty(entry.getValue().toString())) {
                fields.add(entry.getKey());
            }
        }
        for (int i = 0; i < fields.size(); i++) {
            String fieldName = fields.get(i);
            super.remove(fieldName);
        }
    }


    public void removeWith(String prefix) {
        ArrayList<String> names = new ArrayList<>();
        for (Iterator<String> i = this.keySet().iterator(); i.hasNext(); ) {
            String name = i.next();
            if (name.startsWith(prefix)) {
                names.add(name);
            }
        }
        if (names.size() == 0) {
            return;
        }
        String[] names_ = new String[names.size()];
        names.toArray(names_);
        remove(this, names_);
    }


    public void remove(String... fields) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            super.remove(field);
        }
    }

    public void removeExclude(String... fields) {
        HashSet<String> names = new HashSet();
        for (int i = 0; i < fields.length; i++) {
            names.add(fields[i]);
        }
        ArrayList<String> deletes = new ArrayList();
        for (Iterator<Map.Entry<String, Object>> i = this.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Object> entry = i.next();
            if (names.contains(entry.getKey()) == false) {
                deletes.add(entry.getKey());
            }
        }
        if (deletes.size() == 0)
            return;
        String[] deleteFields = new String[deletes.size()];
        deletes.toArray(deleteFields);
        remove(this, deleteFields);
    }


    public void rename(String[] fromNames, String[] toNames) {
        for (int i = 0; i < fromNames.length; i++) {
            if (i >= toNames.length) {
                break;
            }
            if (this.containsKey(fromNames[i]) == false) {
                continue;
            }
            this.append(toNames[i], this.remove(fromNames[i]));
        }
    }

    public void rename(String fromName, String toName) {
        String[] fromNames = fromName.split(",");
        String[] toNames = toName.split(",");
        this.rename(fromNames, toNames);
    }


    public JsonObject parseObject(String fieldName) {
        String content = this.getString(fieldName);
        JsonObject fieldValue = JsonUtil.parse(content);
        this.append(fieldName, fieldValue);
        return fieldValue;
    }

    public JsonObjects parseObjects(String fieldName) {
        String content = this.getString(fieldName);
        JsonObjects fieldValue = JsonUtil.parses(content);
        this.append(fieldName, fieldValue);
        return fieldValue;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public static String[] arrayToStrings(List<String> values) {
        String[] result = new String[values.size()];
        values.toArray(result);
        return result;
    }

    public static JsonObject[] arrayToObjects(List<JsonObject> values) {
        JsonObject[] result = new JsonObject[values.size()];
        values.toArray(result);
        return result;
    }

}
