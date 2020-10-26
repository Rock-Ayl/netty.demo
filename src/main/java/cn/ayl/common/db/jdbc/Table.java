package cn.ayl.common.db.jdbc;

import cn.ayl.config.Const;
import cn.ayl.common.db.jdbc.sqlbuilder.Sql;
import cn.ayl.util.GsonUtils;
import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Table {

    protected static Logger logger = LoggerFactory.getLogger(Table.class);
    protected static Connection connect = init();
    protected static List<Object> VOIDS = new ArrayList();
    protected boolean autoClose;

    protected static class RowObserve implements IObserveRecord {
        protected ListResult rows = new ListResult();
        protected boolean wrapperParam = true;

        protected static RowObserve build(boolean wrapperParam) {
            RowObserve result = new RowObserve();
            result.wrapperParam = wrapperParam;
            return result;
        }

        @Override
        public boolean wrapperParam() {
            return this.wrapperParam;
        }

        @Override
        public boolean parse(ResultSet rs, int fieldCount) {
            try {
                while (rs.next()) {
                    String[] row = new String[fieldCount];
                    rows.add(row);
                    for (int i = 0; i < fieldCount; i++) {
                        if (Types.TIMESTAMP == rs.getMetaData().getColumnType(i + 1)) {
                            Timestamp timestamp = rs.getTimestamp(i + 1);
                            if (timestamp != null) {
                                row[i] = String.valueOf(timestamp.getTime());
                            } else {
                                row[i] = null;
                            }
                            continue;
                        } else if (Types.DATE == rs.getMetaData().getColumnType(i + 1)) {
                            Date date = rs.getDate(i + 1);
                            if (date != null) {
                                row[i] = String.valueOf(date.getTime());
                            } else {
                                row[i] = null;
                            }
                            continue;
                        }
                        row[i] = rs.getString(i + 1);
                        if (StringUtils.isEmpty(row[i])) {
                            row[i] = "";
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                logger.error("getStringData", e);
                return false;
            }
        }
    }

    protected static Connection init() {
        Connection conn = null;
        try {
            Class.forName(Const.JdbcDriver);
            conn = DriverManager.getConnection("jdbc:mysql://" + Const.JdbcHost + ":" + Const.JdbcPort + "/" + Const.JdbcDBName + "?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false&serverTimezone = GMT", Const.JdbcUser, Const.JdbcPassword);
        } catch (Exception e) {
            logger.error("" + e);
        }
        return conn;
    }

    protected List<Object> toList(Object[] values) {
        List<Object> result = new ArrayList();
        for (Object v : values) {
            result.add(v);
        }
        return result;
    }

    protected Object fieldValue(Object v) {
        if (v instanceof JsonObject) {
            return ((JsonObject) v).toJson();
        } else if (v instanceof JsonObjects) {
            return ((JsonObjects) v).toJson();
        } else if (v instanceof List) {
            return GsonUtils.toJson(v);
        } else {
            return v;
        }
    }


    public void updateStatement(PreparedStatement statement, List<Object> values) throws SQLException {
        for (int i = 0; values != null && i < values.size(); i++) {
            Object v = values.get(i);
            statement.setObject(i + 1, fieldValue(v));
        }
    }

    public void updateStatement(PreparedStatement statement, Object[] values) throws SQLException {
        for (int i = 0; values != null && i < values.length; i++) {
            Object v = values[i];
            statement.setObject(i + 1, fieldValue(v));
        }
    }

    public void close() {
        try {
            connect.close();
        } catch (Exception e) {
            logger.error("Connection", e);
        }
    }

    public void close(ResultSet rs, PreparedStatement statement) {
        this.close(statement);
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
        }
    }

    public void close(PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
        }
    }

    private void printParamError(Object values) {
        StringBuilder buf = new StringBuilder();
        Object[] items;
        if (values instanceof List) {
            List<?> params1 = (List) values;
            items = new Object[params1.size()];
            for (int i = 0; i < params1.size(); i++) {
                items[i] = params1.get(i);
                buf.append("paramIndex[" + i + "]= {}\n");
            }
        } else {
            items = (Object[]) values;
            for (int i = 0; i < items.length; i++) {
                buf.append("paramIndex[" + i + "]= {}\n");
            }
        }
        logger.error(buf.toString(), items);
    }

    public long insert(Sql sql) {
        return doInsert(sql.toString(), sql.parameters());
    }

    public long insert(String sql, List<Object> values) {
        return doInsert(sql, values);
    }

    public long insert(String sql, Object... values) {
        return doInsert(sql, values);
    }

    private long doInsert(String sql, Object values) {
        long AutoId = -1;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement(sql);
            if (values instanceof List) {
                updateStatement(statement, (List) values);
            } else {
                updateStatement(statement, (Object[]) values);
            }
            statement.execute();
            rs = statement.executeQuery("SELECT LAST_INSERT_ID()");
            if (rs.next()) {
                AutoId = rs.getLong(1);
            }
        } catch (Exception e) {
            logger.error(sql, e);
            this.printParamError(values);
            throw new RuntimeException(e);
        } finally {
            close(rs, statement);
            if (this.autoClose) close();
        }
        return AutoId;
    }

    private PreparedStatement createStatement(String sql) {
        try {
            return connect.prepareStatement(sql);
        } catch (Exception e) {
            logger.error(sql, e);
            return null;
        }
    }

    public boolean update(String sql, Object... values) {
        PreparedStatement statement = createStatement(sql);
        return runStatement(sql, statement, values);
    }

    public boolean update(Sql sql) {
        return this.update(sql.toString(), sql.parameters());
    }

    public boolean update(String sql, List<Object> values) {
        PreparedStatement statement = createStatement(sql);
        if (statement == null) return false;
        return runStatement(sql, statement, values);
    }

    private boolean runStatement(String sql, PreparedStatement statement, Object values) throws RuntimeException {
        try {
            if (values instanceof List) {
                updateStatement(statement, (List<Object>) values);
            } else {
                updateStatement(statement, (Object[]) values);
            }
            statement.execute();
            return true;
        } catch (Exception e) {
            logger.error(sql, e);
            this.printParamError(values);
            throw new RuntimeException(e);
        } finally {
            close(statement);
            if (this.autoClose) close();
        }
    }

    public String[] findOne(String sql, Object[] params) {
        List<String[]> result = query(sql, this.toList(params), 0, 0, false);
        if (result.size() == 0) return null;
        return result.get(0);
    }

    public ListResult query(String sql, int offset, int count) {
        return query(sql, VOIDS, offset, count, true);
    }

    public ListResult query(Sql sql, int offset, int count) {
        return this.query(sql.toString(), sql.parameters(), offset, count);
    }

    public ListResult query(Sql sql) {
        return this.query(sql.toString(), sql.parameters());
    }

    public ListResult query(String sql, List<Object> params) {
        return query(sql, params, 0, 0, true);
    }

    public ListResult query(String sql, Object[] params) {
        return query(sql, this.toList(params), 0, 0, false);
    }

    public ListResult query(String sql, Object[] params, int offset, int count) {
        return query(sql, this.toList(params), offset, count, false);
    }

    public ListResult query(String sql, List<Object> params, int offset, int count) {
        return query(sql, params, offset, count, true);
    }

    public ListResult query(String sql, List<Object> params, int offset, int count, boolean wrapperParam) {
        RowObserve observe = RowObserve.build(wrapperParam);
        execute(sql, params, offset, count, observe);
        return observe.rows;
    }

    protected boolean execute(String sql, List<Object> values, int offset, int count, IObserveRecord observe) throws RuntimeException {
        List<Object> params = observe.wrapperParam() ? new ArrayList(values) : values;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder q = new StringBuilder();
            q.append(sql);
            if (count != 0) {
                q.append(" limit ? offset ? ");
                params.add(count);
                params.add(offset);
            }
            stmt = connect.prepareStatement(q.toString());
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
            }
            if (observe.wrapperParam()) params.clear();
            rs = stmt.executeQuery();
            return observe.parse(rs, rs.getMetaData().getColumnCount());
        } catch (Exception e) {
            logger.error(sql, e);
            throw new RuntimeException(e);
        } finally {
            close(rs, stmt);
            if (this.autoClose) close();
        }
    }

    public String get(String sql, List<Object> params, boolean wrapperParam) {
        List<String[]> result = query(sql, params, 0, 0, wrapperParam);
        if (result.size() == 0) return null;
        return result.get(0)[0];
    }

    public String get(String sql, Object[] params) {
        return this.get(sql, this.toList(params), false);
    }

    public int getInt(Sql sql) {
        String result = this.get(sql.toString(), sql.parameters(), false);
        return result == null ? 0 : Integer.parseInt(result);
    }

    public int getInt(String sql, List<Object> params) {
        String result = this.get(sql, params, true);
        return result == null ? 0 : Integer.parseInt(result);
    }


    public int getInt(String sql, Object[] params) {
        String result = this.get(sql, this.toList(params), false);
        return result == null ? 0 : Integer.parseInt(result);
    }

    public long getLong(Sql sql) {
        String result = this.get(sql.toString(), sql.parameters(), false);
        return result == null ? 0 : Long.parseLong(result);
    }

    public long getLong(String sql, Object[] params) {
        String result = this.get(sql, this.toList(params), false);
        return result == null ? 0 : Long.parseLong(result);
    }

    public boolean exist(String tableName, String whereSql, Object[] params) {
        String sql = "select count(*) from " + tableName + " where " + whereSql;
        List<String[]> rows = query(sql, params, 0, 0);
        return Integer.parseInt(rows.get(0)[0]) > 0;
    }

}
