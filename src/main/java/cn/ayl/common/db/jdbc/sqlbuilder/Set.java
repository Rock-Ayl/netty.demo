package cn.ayl.common.db.jdbc.sqlbuilder;

public class Set extends Sentence {

    private int count = 0;

    public Set(Sql sql) {
        super(sql, "set");
    }

    public Set add(String fieldName) {
        return (Set) super.add(false, count >= 2, "=", fieldName);
    }

    public Set addValue(String fieldName, Object fieldValue) {
        return (Set) super.addValue(fieldName, "=", fieldValue);
    }

    public Set addParam(String fieldName, Object fieldValue) {
        count++;
        this.add(fieldName);
        this.sql.addParamValue(fieldName, fieldValue);
        return this;
    }
}
