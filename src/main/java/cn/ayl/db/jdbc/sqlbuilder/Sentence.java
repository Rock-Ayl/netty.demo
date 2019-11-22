package cn.ayl.db.jdbc.sqlbuilder;

import cn.ayl.config.Const;

public class Sentence {

    protected StringBuilder buf = new StringBuilder();
    protected Sql sql;

    public Sentence(Sql sql, String content) {
        this.sql = sql;
        buf.append(Const.SPACE);
        buf.append(content);
    }

    protected Sentence append(String content) {
        buf.append(Const.SPACE);
        buf.append(content);
        return this;
    }

    private void addCommon(boolean hasEnclose, boolean hasDot, String fieldName) {
        buf.append(Const.SPACE);
        if (hasEnclose) {
            buf.append("(");
        }
        if (hasDot) {
            buf.append(",");
        }
        buf.append(fieldName);
    }

    public Sentence add(boolean hasEnclose, boolean hasDot, String logical, String fieldName) {
        addCommon(hasEnclose, hasDot, fieldName);
        buf.append(Const.SPACE);
        buf.append(logical);
        buf.append(Const.SPACE);
        buf.append("?");
        if (hasEnclose) {
            buf.append(")");
        }
        return this;
    }


    public Sentence addValue(String fieldName, String logical, Object fieldValue) {
        buf.append(Const.SPACE);
        buf.append("(");
        buf.append(fieldName);
        buf.append(Const.SPACE);
        buf.append(logical);
        buf.append(Const.SPACE);
        if (fieldName instanceof String) {
            buf.append("'");
            buf.append(fieldValue);
            buf.append("'");
        } else {
            buf.append(fieldValue);
        }
        buf.append(")");
        return this;
    }

    public Sql build() {
        return this.sql;
    }

    public String toString() {
        return this.buf.toString();
    }

}

