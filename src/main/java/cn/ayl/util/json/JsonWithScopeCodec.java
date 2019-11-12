package cn.ayl.util.json;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class JsonWithScopeCodec implements Codec<JsonWithScope> {
    private final Codec<JsonObject> documentCodec;

    public JsonWithScopeCodec(final Codec<JsonObject> documentCodec) {
        this.documentCodec = documentCodec;
    }

    @Override
    public JsonWithScope decode(final BsonReader bsonReader, final DecoderContext decoderContext) {
        String code = bsonReader.readJavaScriptWithScope();
        JsonObject scope = documentCodec.decode(bsonReader, decoderContext);
        return new JsonWithScope(code, scope);
    }

    @Override
    public void encode(final BsonWriter writer, final JsonWithScope codeWithScope, final EncoderContext encoderContext) {
        writer.writeJavaScriptWithScope(codeWithScope.getCode());
        documentCodec.encode(writer, codeWithScope.getScope(), encoderContext);
    }

    @Override
    public Class<JsonWithScope> getEncoderClass() {
        return JsonWithScope.class;
    }
}
