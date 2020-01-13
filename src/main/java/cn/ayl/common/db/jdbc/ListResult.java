package cn.ayl.common.db.jdbc;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ListResult extends ArrayList<String[]> {

    public String string(int recordIndex, int fieldIndex) {
        return this.get(recordIndex)[fieldIndex];
    }

    public int asInt(int recordIndex, int fieldIndex) {
        return Integer.parseInt(this.string(recordIndex, fieldIndex));
    }

    public long asLong(int recordIndex, int fieldIndex) {
        return Long.parseLong(this.string(recordIndex, fieldIndex));
    }

    public float asFloat(int recordIndex, int fieldIndex) {
        return Float.parseFloat(this.string(recordIndex, fieldIndex));
    }

    public double asDouble(int recordIndex, int fieldIndex) {
        return Double.parseDouble(this.string(recordIndex, fieldIndex));
    }

    public boolean asBool(int recordIndex, int fieldIndex) {
        String v = this.string(recordIndex, fieldIndex);
        if (v.equalsIgnoreCase("true")) {
            return true;
        }
        if (StringUtils.isNumeric(v) && Integer.parseInt(v) > 0) {
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

}
