package cn.ayl.common.json;

import org.apache.commons.lang3.StringUtils;
import org.bson.AbstractBsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static JsonCodec parser = new JsonCodec();

    protected static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    public static JsonObject merge(JsonObject source, JsonObject dest) {
        boolean b1 = source == null;
        boolean b2 = dest == null;
        if (b1 && b2)
            return null;
        if (b1 == false && b2)
            return source;
        if (b2 == false && b1)
            return dest;
        source.putAll(dest);
        return source;
    }


    public static void copy(Map<String, ?> oSource, Map<String, Object> oDest, String... fields) {
        if (oSource == null)
            return;
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            Object o = oSource.get(field);
            if (o == null)
                continue;
            oDest.put(field, o);
        }
    }


    public static void copy(JsonObject oSource1, JsonObject oSource2, JsonObject oDest, String field) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(oDest.getString(field)) == false) {
            return;
        }
        if (oSource1 != null && org.apache.commons.lang3.StringUtils.isEmpty(oSource1.getString(field)) == false) {
            oDest.append(field, oSource1.getString(field));
            return;
        }
        if (oSource2 != null && org.apache.commons.lang3.StringUtils.isEmpty(oSource2.getString(field)) == false) {
            oDest.append(field, oSource2.getString(field));
        }
    }


    public static void copy(Map<String, Object> oSource, String sourceField, Map<String, Object> oDest, String destField) {
        Object value = oSource.get(sourceField);
        if (value == null)
            return;
        if ((value instanceof String) && (org.apache.commons.lang3.StringUtils.isEmpty((String) value))) {
            return;
        }
        oDest.put(destField, value);
    }


    public static String toString(List<?> list, String fieldName) {
        if (list == null)
            return "";
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof String) {
                if (buffer.length() > 0) {
                    buffer.append(",");
                }
                buffer.append(list.get(i).toString());
            } else if (org.apache.commons.lang3.StringUtils.isEmpty(fieldName) == false && ((JsonObject) list.get(i)).containsKey(fieldName)) {
                if (buffer.length() > 0) {
                    buffer.append(",");
                }
                buffer.append(((JsonObject) list.get(i)).getString(fieldName));
            }
        }
        return buffer.toString();
    }

    public static String toJson(JsonList list) {
        if (list.isJsonObject()) {
            return list.objects.toJson();
        }
        StringWriter writer = new StringWriter();
        BsonDocWriter w = new BsonDocWriter(writer);
        try {
            w.updateState(AbstractBsonWriter.State.VALUE);
            parser.writeIterable(w, list.values, EncoderContext.builder().build());
            String content = writer.toString();
            return content;
        } finally {
            w.close();
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static String toJson(JsonObject document) {
        StringWriter writer = new StringWriter();
        BsonDocWriter w = new BsonDocWriter(writer);
        try {
            parser.encode(w, document, EncoderContext.builder().build());
            String content = writer.toString();
            return content;
        } finally {
            w.close();
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }


    public static String toJson(JsonObjects documents) {
        StringWriter writer = new StringWriter();
        BsonDocWriter w = new BsonDocWriter(writer);
        try {
            parser.writeDocs(w, documents, EncoderContext.builder().build());
            String content = writer.toString();
            return content;
        } finally {
            w.close();
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }


    public static String parse(String content, String fieldName) {
        content = content.replaceAll("\n", "");
        JsonObjects items = parses(content);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; items != null && i < items.size(); i++) {
            Object oItem = items.get(i);
            String value = null;
            if (oItem instanceof String) {
                value = (String) oItem;
            } else if (oItem instanceof JsonObject) {
                value = ((JsonObject) oItem).getString(fieldName);
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(value))
                continue;
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(value);
        }
        return buf.toString();
    }


    public static JsonObject parse(JsonObject obj, String fieldName) {
        String content = obj.getString(fieldName);
        if (org.apache.commons.lang3.StringUtils.isEmpty(content)) {
            return new JsonObject();
        }
        return parse(content);
    }


    public static JsonObject parse(String content) {
        if (StringUtils.isEmpty(content)) return new JsonObject();
        content = content.replaceAll("\n", "");
        JsonReader reader = new JsonReader(content);
        try {
            return parser.decode(reader, DecoderContext.builder().build());
        } catch (Exception e) {
            logger.error("parse[{}]", content, e);
            throw e;
        } finally {
            reader.close();
        }
    }

    public static JsonObjects parses(String content) {
        if (StringUtils.isEmpty(content)) return new JsonObjects();
        content = content.replaceAll("\n", "");
        JsonReader reader = new JsonReader(content);
        try {
            return parser.parseDocs(reader, DecoderContext.builder().build());
        } catch (Exception e) {
            logger.error("parses[{}]", content, e);
            throw e;
        } finally {
            reader.close();
        }
    }

    public static JsonObjects parses(JsonObject obj, String fieldName) {
        Object o = obj.get(fieldName);
        if (o == null) {
            return new JsonObjects();
        }
        if (o instanceof String) {
            return parses((String) o);
        } else {
            return (JsonObjects) obj.get(fieldName);
        }
    }


    public static List<String> parseStrings(String content) {
        return parseList(content).strings();
    }

    public static List<Integer> parseIntegers(String content) {
        return parseList(content).integers();
    }

    public static List<Long> parseLongs(String content) {
        return parseList(content).longs();
    }

    public static List<Float> parseFloats(String content) {
        return parseList(content).floats();
    }

    private static <T> JsonList<T> parseList(String content) {
        if (StringUtils.isEmpty(content)) return JsonList.create();
        content = content.replaceAll("\n", "");
        JsonReader reader = new JsonReader(content);
        try {
            JsonList result = parser.parseList(reader, DecoderContext.builder().build());
            if (result.isNull()) {
                result.values = new ArrayList();
            }
            return result;
        } finally {
            reader.close();
        }
    }

}
