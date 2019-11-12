package cn.ayl.util.json;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonObjects extends ArrayList<JsonObject> {

    public static JsonObjects VOID() {
        return new JsonObjects();
    }

    public JsonObject map(String keyField) {
        JsonObject result = new JsonObject();
        for (JsonObject o : this) {
            String key = (String) o.remove(keyField);
            result.append(key, o);
        }
        return result;
    }

    public HashMap<Long, JsonObject> hashMap(String keyFieldName) {
        HashMap<Long, JsonObject> result = new HashMap();
        for (int i = 0; i < this.size(); i++) {
            JsonObject oItem = this.get(i);
            result.put(oItem.getLong(keyFieldName), oItem);
        }
        return result;
    }


    public JsonObject find(String fieldName, String fieldValue) {
        for (int i = 0; i < this.size(); i++) {
            JsonObject item = this.get(i);
            if (item.getString(fieldName).equals(fieldValue)) {
                return item;
            }
        }
        return null;
    }


    public void removeEmpty(String fieldName) {
        int index = 0;
        while (index < this.size()) {
            JsonObject item = this.get(index);
            String value = item.get(fieldName, "");
            if (StringUtils.isEmpty(value)) {
                this.remove(index);
                continue;
            }
            index++;
        }
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }



}
