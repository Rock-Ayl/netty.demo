package cn.ayl.common.db.jdbc;

import cn.ayl.common.db.jdbc.sqlbuilder.Sql;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Jdbc核心
 * sql实现
 */
public class SqlTable extends Table {

    protected static Logger logger = LoggerFactory.getLogger(SqlTable.class);

    protected static class ObjectObserve implements IObserveRecord {

        public JsonObjects records = JsonObjects.VOID();
        protected boolean wrapperParam = true;

        protected static ObjectObserve build(boolean wrapperParam) {
            ObjectObserve result = new ObjectObserve();
            result.wrapperParam = wrapperParam;
            return result;
        }

        @Override
        public boolean wrapperParam() {
            return this.wrapperParam;
        }

        protected void parseRecord(ResultSet rs, ResultSetMetaData meta, int fieldCount, Map<String, Object> map) {
            try {
                for (int i = 0; i < fieldCount; i++) {
                    int index = i + 1;
                    int type = meta.getColumnType(index);
                    String fieldName = meta.getColumnLabel(index);
                    switch (type) {
                        case Types.INTEGER:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                            map.put(fieldName, rs.getInt(index));
                            break;
                        case Types.TIMESTAMP:
                            Timestamp timestamp = rs.getTimestamp(index);
                            if (timestamp != null) {
                                map.put(fieldName, timestamp.getTime());
                            } else {
                                map.put(fieldName, 0L);
                            }
                            break;
                        case Types.DATE:
                            Date date = rs.getDate(index);
                            if (date != null) {
                                map.put(fieldName, date.getTime());
                            } else {
                                map.put(fieldName, 0);
                            }
                            break;
                        case Types.BIGINT:
                        case Types.ROWID:
                            map.put(fieldName, rs.getLong(index));
                            break;
                        case Types.NUMERIC:
                        case Types.FLOAT:
                        case Types.REAL:
                        case Types.DECIMAL:
                            map.put(fieldName, rs.getFloat(index));
                            break;
                        case Types.DOUBLE:
                            map.put(fieldName, rs.getDouble(index));
                            break;
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                        case Types.SQLXML:
                            String value = rs.getString(index);
                            map.put(fieldName, StringUtils.isEmpty(value) ? "" : value);
                            break;
                        case Types.BOOLEAN:
                            map.put(fieldName, rs.getBoolean(index));
                            break;
                    }
                }
            } catch (Exception e) {

            }
        }

        @Override
        public boolean parse(ResultSet rs, int fieldCount) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    JsonObject record = JsonObject.VOID();
                    this.parseRecord(rs, meta, fieldCount, record);
                    records.add(record);
                }
                return true;
            } catch (Exception e) {
                logger.error("readObjects", e);
                return false;
            }
        }
    }

    public static SqlTable use() {
        return new SqlTable();
    }

    public JsonObject queryObject(Sql sql) {
        if (sql.paramCount() == 0) {
            return this.queryObject(sql.toString());
        } else {
            return this.queryObject(sql.toString(), sql.parameters());
        }
    }

    public JsonObject queryObject(String sql) {
        JsonObjects list = queryObjects(sql, 0, 1);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public JsonObject queryObject(String sql, Object[] values) {
        JsonObjects list = queryObjects(sql, this.toList(values), 0, 1, false);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public JsonObject queryObject(String sql, List<Object> values) {
        JsonObjects list = queryObjects(sql, values, 0, 1, true);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public JsonObjects queryObjects(Sql sql) {
        return this.queryObjects(sql.toString(), sql.parameters(), 0, 0, true);
    }

    public JsonObjects queryObjects(String sql) {
        return queryObjects(sql, this.toList(new Object[]{}), 0, 0, false);
    }

    public JsonObjects queryObjects(String sql, List<Object> values) {
        return queryObjects(sql, values, 0, 0, true);
    }

    public JsonObjects queryObjects(String sql, Object[] values) {
        return queryObjects(sql, this.toList(values), 0, 0, false);
    }

    public JsonObjects queryObjects(Sql sql, int offset, int count) {
        return queryObjects(sql.toString(), sql.parameters(), offset, count, true);
    }

    public JsonObjects queryObjects(String sql, int offset, int count) {
        return queryObjects(sql, VOIDS, offset, count, true);
    }

    public JsonObjects queryObjects(String sql, Object[] values, int offset, int count) {
        return this.queryObjects(sql, toList(values), offset, count, false);
    }

    public JsonObjects queryObjects(String sql, List<Object> values, int offset, int count) {
        return this.queryObjects(sql, values, offset, count, true);
    }

    public JsonObjects queryObjects(String sql, List<Object> params, int offset, int count, boolean wrapperParam) {
        ObjectObserve observe = ObjectObserve.build(wrapperParam);
        execute(sql, params, offset, count, observe);
        return observe.records;
    }

}
