package cn.ayl.db.jdbc.sqlbuilder;

import cn.ayl.config.Const;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Sql {

    private StringBuilder buf = new StringBuilder();
    private Select select = null;
    private Where where = null;
    private Order order = null;
    private Set set = null;
    private Sentence from = null;
    private Values values = null;
    protected LinkedHashMap<String, Object> params = new LinkedHashMap();

    public Sql() {

    }


    public static Sql create(String content){
        Sql result=new Sql();
        result.append(content);
        return result;
    }

    public Sql append(String content) {
        buf.append(Const.SPACE);
        buf.append(content);
        return this;
    }

    protected void addParamValue(String name, Object value) {
        this.setParamValue(name, value);
    }

    public void setParamValue(String name, Object value) {
        params.put(name, value);
    }

    public int paramCount() {
        return this.params.size();
    }

    public List<Object> parameters() {
        List<Object> result = new ArrayList();
        for (Object v : params.values()) {
            result.add(v);
        }
        return result;
    }

    public Sql select() {
        return this.select(null);
    }

    public Sql select(String... fields) {
        if (select == null) {
            select = new Select(this);
        }
        if (fields != null) {
            select.setFields(fields);
        }
        return this;
    }

    public Order order() {
        if (order == null) {
            order = new Order(this);
        }
        return order;
    }

    public Sql from(String... tableNames) {
        if (from == null) {
            from = new Sentence(this, Const.SPACE);
            from.append("from ");
        }
        for (int i = 0; i < tableNames.length; i++) {
            if (i >= 1) from.append(",");
            from.append(tableNames[i]);
        }
        return this;
    }

    public Where where() {
        if (where == null) {
            where = new Where(this);
        }
        return where;
    }

    public Where where(String content, Object[] params) {
        if (where == null) {
            where = new Where(this);
        }
        where.append(content);
        int index = 0;
        for (Object param : params) {
            this.addParamValue(String.valueOf(index), param);
        }
        return where;
    }

    public Set set() {
        if (set == null) {
            set = new Set(this);
        }
        return set;
    }

    public Values values() {
        if (values == null) {
            values = new Values(this);
        }
        return values;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.buf);
        if (select != null) {
            if (result.length() == 0) {
                result.append("select");
                result.append(Const.SPACE);
            }
            result.append(select.toString());
        }
        if (from != null) result.append(from.toString());
        if (set != null) result.append(set.toString());
        if (values != null) result.append(values.toString());
        if (where != null) result.append(where.toString());
        if (order != null) result.append(order.toString());
        return result.toString();
    }


}
