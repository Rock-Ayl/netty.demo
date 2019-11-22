package cn.ayl.db.jdbc.sqlbuilder;

public class Where extends Sentence {

    protected int paramCount = 0;

    public Where(Sql sql) {
        super(sql, "where");
    }

    public Where add(String fieldName) {
        return (Where) super.add(true, false, "=", fieldName);
    }

    public Where addValue(String fieldName, Object fieldValue) {
        return (Where) super.addValue(fieldName, "=", fieldValue);
    }

    public Where addValue(String fieldName, String logical, Object fieldValue) {
        return (Where) super.addValue(fieldName, logical, fieldValue);
    }


    public Where addParam(String fieldName, Object fieldValue) {
        return this.addParam(fieldName, "=", fieldValue);
    }

    public Where addParam(String fieldName, String logical, Object fieldValue) {
        return (Where) this.addParam(true, false, fieldName, logical, fieldValue);
    }

    private Sentence addParam(boolean hasEnclose, boolean hasDot, String fieldName, String logical, Object fieldValue) {
        this.add(hasEnclose, hasDot, logical, fieldName);
        this.sql.addParamValue(fieldName, fieldValue);
        paramCount += 1;
        return this;
    }

    public Where and(String fieldName) {
        this.append("and");
        return add(fieldName);
    }

    public Where andValue(String fieldName, Object fieldValue) {
        this.append("and");
        return addValue(fieldName, fieldValue);
    }

    public Where andParam(String fieldName, Object fieldValue) {
        this.append("and");
        return addParam(fieldName, fieldValue);
    }

    public Where andParam(String fieldName, String logical, Object fieldValue) {
        this.append("and");
        return addParam(fieldName, logical, fieldValue);
    }


    public Where or(String fieldName) {
        this.append("or");
        return add(fieldName);
    }

    public Where orValue(String fieldName, Object fieldValue) {
        return this.orValue(fieldName, "=", fieldValue);
    }

    public Where orValue(String fieldName, String logical, Object fieldValue) {
        this.append("or");
        return addValue(fieldName, logical, fieldValue);
    }

    public Where orParam(String fieldName, Object fieldValue) {
        return this.orParam(fieldName, "=", fieldValue);
    }

    public Where orParam(String fieldName, String logical, Object fieldValue) {
        this.append("or");
        return addParam(fieldName, logical, fieldValue);
    }

}
