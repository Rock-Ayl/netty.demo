package cn.ayl.json;

import org.bson.BsonType;
import org.bson.Transformer;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.CodeWithScope;

import java.util.HashMap;
import java.util.Map;

import static org.bson.assertions.Assertions.notNull;

public class JsonCodecProvider implements CodecProvider {
    private final BsonTypeClassMap bsonTypeClassMap;
    private final Transformer valueTransformer;
    public static Map<BsonType, Class<?>> replacementsForDefaults=new HashMap();
    public static BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP ;

    static {
        replacementsForDefaults.put(BsonType.DOCUMENT, JsonObject.class);
        replacementsForDefaults.put(BsonType.ARRAY, JsonObjects.class);
        DEFAULT_BSON_TYPE_CLASS_MAP=new BsonTypeClassMap(replacementsForDefaults);
    }
    /**
     * Construct a new instance with a default {@code BsonTypeClassMap}.
     */
    public JsonCodecProvider() {
        this(DEFAULT_BSON_TYPE_CLASS_MAP);
    }

    public JsonCodecProvider(final Transformer valueTransformer) {
        this(DEFAULT_BSON_TYPE_CLASS_MAP, valueTransformer);
    }

    public JsonCodecProvider(final BsonTypeClassMap bsonTypeClassMap) {
        this(bsonTypeClassMap, null);
    }

    public JsonCodecProvider(final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.bsonTypeClassMap = notNull("bsonTypeClassMap", bsonTypeClassMap);
        this.valueTransformer = valueTransformer;
    }

    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (clazz == CodeWithScope.class) {
            return (Codec<T>) new JsonWithScopeCodec(registry.get(JsonObject.class));
        }
        if (clazz == JsonObject.class) {
            return (Codec<T>) new JsonCodec(registry, bsonTypeClassMap, valueTransformer);
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonCodecProvider that = (JsonCodecProvider) o;

        if (!bsonTypeClassMap.equals(that.bsonTypeClassMap)) {
            return false;
        }
        if (valueTransformer != null ? !valueTransformer.equals(that.valueTransformer) : that.valueTransformer != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = bsonTypeClassMap.hashCode();
        result = 31 * result + (valueTransformer != null ? valueTransformer.hashCode() : 0);
        return result;
    }
}
