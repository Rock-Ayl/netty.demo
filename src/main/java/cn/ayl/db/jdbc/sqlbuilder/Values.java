package cn.ayl.db.jdbc.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public class Values {

    protected List<String> fields = new ArrayList();
    protected Sql sql;

    public Values(Sql sql) {
        this.sql = sql;
    }

    public Values addParam(String fieldName, Object fieldValue) {
        fields.add(fieldName);
        this.sql.addParamValue(fieldName,fieldValue);
        return this;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < fields.size(); i++) {
            builder.append(fields.get(i));
            if (i < fields.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")values(");
        for (int i = 0; i < fields.size(); i++) {
            builder.append("?");
            if (i < fields.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

}
