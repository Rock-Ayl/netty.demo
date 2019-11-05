package cn.ayl.json;

import org.bson.types.Code;

/**
 * A representation of the JavaScript Code with Scope BSON type.
 *
 * @since 3.0
 */
public class JsonWithScope extends Code {

    private final JsonObject scope;

    private static final long serialVersionUID = -6284832275113680002L;

    /**
     * Construct an instance.
     *
     * @param code the code
     * @param scope the scope
     */
    public JsonWithScope(final String code, final JsonObject scope) {
        super(code);
        this.scope = scope;
    }

    /**
     * Gets the scope, which is is a mapping from identifiers to values, representing the scope in which the code should be evaluated.
     *
     * @return the scope
     */
    public JsonObject getScope() {
        return scope;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        JsonWithScope that = (JsonWithScope) o;

        if (scope != null ? !scope.equals(that.scope) : that.scope != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getCode().hashCode() ^ scope.hashCode();
    }
}

