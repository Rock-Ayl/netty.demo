
package cn.ayl.common.json;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.bson.assertions.Assertions.notNull;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * DocumentCodec的逻辑
 **/
public class JsonCodec implements CollectibleCodec<JsonObject> {
    private static final String ID_FIELD_NAME = "_id";
    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(asList(new ValueCodecProvider(),
            new BsonValueCodecProvider(),
            new JsonCodecProvider()));

    private final BsonTypeCodecMap bsonTypeCodecMap;
    private final CodecRegistry registry;
    private final IdGenerator idGenerator;
    private final Transformer valueTransformer;

    /**
     * Construct a new instance with a default {@code CodecRegistry}.
     */
    public JsonCodec() {
        this(DEFAULT_REGISTRY);
    }

    /**
     * Construct a new instance with the given registry.
     *
     * @param registry the registry
     * @since 3.5
     */
    public JsonCodec(final CodecRegistry registry) {
        this(registry, JsonCodecProvider.DEFAULT_BSON_TYPE_CLASS_MAP);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map.
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     */
    public JsonCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap) {
        this(registry, bsonTypeClassMap, null);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map. The transformer is applied as a last step when decoding
     * values, which allows users of this codec to control the decoding process.  For example, a user of this class could substitute a
     * value decoded as a JsonObject with an instance of a special purpose class (e.g., one representing a DBRef in MongoDB).
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     * @param valueTransformer the value transformer to use as a final step when decoding the value of any field in the JsonObject
     */
    public JsonCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.registry = notNull("registry", registry);
        this.bsonTypeCodecMap = new BsonTypeCodecMap(notNull("bsonTypeClassMap", bsonTypeClassMap), registry);
        this.idGenerator = new ObjectIdGenerator();
        this.valueTransformer = valueTransformer != null ? valueTransformer : new Transformer() {
            @Override
            public Object transform(final Object value) {
                return value;
            }
        };
    }

    @Override
    public boolean documentHasId(final JsonObject JsonObject) {
        return JsonObject.containsKey(ID_FIELD_NAME);
    }

    @Override
    public BsonValue getDocumentId(final JsonObject JsonObject) {
        if (!documentHasId(JsonObject)) {
            throw new IllegalStateException("The JsonObject does not contain an _id");
        }

        Object id = JsonObject.get(ID_FIELD_NAME);
        if (id instanceof BsonValue) {
            return (BsonValue) id;
        }

        BsonDocument idHoldingDocument = new BsonDocument();
        BsonWriter writer = new BsonDocumentWriter(idHoldingDocument);
        writer.writeStartDocument();
        writer.writeName(ID_FIELD_NAME);
        writeValue(writer, EncoderContext.builder().build(), id);
        writer.writeEndDocument();
        return idHoldingDocument.get(ID_FIELD_NAME);
    }

    @Override
    public JsonObject generateIdIfAbsentFromDocument(final JsonObject JsonObject) {
        if (!documentHasId(JsonObject)) {
            JsonObject.put(ID_FIELD_NAME, idGenerator.generate());
        }
        return JsonObject;
    }

    @Override
    public void encode(final BsonWriter writer, final JsonObject JsonObject, final EncoderContext encoderContext) {
        writeMap(writer, JsonObject, encoderContext);
    }

    @Override
    public JsonObject decode(final BsonReader reader, final DecoderContext decoderContext) {
        JsonObject result = new JsonObject();

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            result.put(fieldName, readValue(reader, decoderContext));
        }

        reader.readEndDocument();

        return result;
    }

    @Override
    public Class<JsonObject> getEncoderClass() {
        return JsonObject.class;
    }

    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final Map<String, Object> JsonObject) {
        if (encoderContext.isEncodingCollectibleDocument() && JsonObject.containsKey(ID_FIELD_NAME)) {
            bsonWriter.writeName(ID_FIELD_NAME);
            writeValue(bsonWriter, encoderContext, JsonObject.get(ID_FIELD_NAME));
        }
    }

    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals(ID_FIELD_NAME);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final Object value) {
        Object object;
        if (value instanceof Iterable) {
            writeIterable(writer, (Iterable<Object>) value, encoderContext.getChildContext());
        } else if (value instanceof Map) {
            writeMap(writer, (Map<String, Object>) value, encoderContext.getChildContext());
        } else {
            Class c = value.getClass();
            Codec codec;
            if (c.isArray()) {
                writeArray(writer, (Object[]) value, encoderContext.getChildContext());
            } else if (c == Long.class) {
                object = new BsonInt64((long) value);
                codec = registry.get(BsonInt64.class);
                encoderContext.encodeWithChildContext(codec, writer, object);
            } else {
                codec = registry.get(value.getClass());
                encoderContext.encodeWithChildContext(codec, writer, value);
            }
        }
    }

    public void writeMap(final BsonWriter writer, final Map<String, Object> map, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        beforeFields(writer, encoderContext, map);
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (skipField(encoderContext, entry.getKey()) || (entry.getValue() == null)) {
                continue;
            }
            writer.writeName(entry.getKey());
            writeValue(writer, encoderContext, entry.getValue());
        }
        writer.writeEndDocument();
    }

    public void writeIterable(final BsonWriter writer, final Iterable<Object> list, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object value : list) {
            writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }

    public void writeArray(final BsonWriter writer, final Object[] list, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object value : list) {
            writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }


    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.ARRAY) {
            return readList(reader, decoderContext);
        } else if (bsonType == BsonType.BINARY && BsonBinarySubType.isUuid(reader.peekBinarySubType()) && reader.peekBinarySize() == 16) {
            return registry.get(UUID.class).decode(reader, decoderContext);
        }
        return valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext));
    }

    public void writeDocs(final BsonDocWriter writer, final JsonObjects list, final EncoderContext encoderContext) {
        writer.updateState(AbstractBsonWriter.State.VALUE);
        writer.writeStartArray();
        for (final JsonObject value : list) {
            writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }

    public <T> List<T> readList(final BsonReader reader, final DecoderContext decoderContext) {
        JsonList<T> result = this.parseList(reader, decoderContext);
        if (result.isJsonObject()) {
            return (List<T>) result.objects;
        } else if (!result.isNull()) {
            return result.values;
        } else {
            return new ArrayList<T>();
        }
    }


    public <T> JsonList<T> parseList(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        JsonList<T> results = new JsonList();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            if (results.isNull()) {
                results.init(reader.getCurrentBsonType());
            }
            Object v = readValue(reader, decoderContext);
            results.add(v);
        }
        reader.readEndArray();
        return results;
    }

    public JsonObjects parseDocs(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        JsonObjects list = new JsonObjects();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add((JsonObject) readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return list;
    }

}
