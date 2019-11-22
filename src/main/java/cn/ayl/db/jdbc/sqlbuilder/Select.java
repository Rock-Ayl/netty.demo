package cn.ayl.db.jdbc.sqlbuilder;

import cn.ayl.config.Const;

public class Select extends Sentence {

    protected int fieldCount = 0;

    public Select(Sql sql) {
        super(sql, "");
    }

    public Select addFields(String... fieldNames) {
        if (fieldCount > 0) buf.append(",");
        buf.append(Const.SPACE);
        for (int i = 0; i < fieldNames.length; i++) {
            if (i >= 1) buf.append(",");
            buf.append(fieldNames[i]);
        }
        fieldCount += fieldNames.length;
        return this;
    }

    public void setFields(String... fieldNames) {
        buf.setLength(0);
        fieldCount = 0;
        addFields(fieldNames);
    }

}