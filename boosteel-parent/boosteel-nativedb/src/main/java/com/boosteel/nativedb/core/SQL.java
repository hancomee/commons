package com.boosteel.nativedb.core;


import com.boosteel.util.IAccess;

import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.boosteel.nativedb.core.DynamicQuery.toQuery;
import static com.boosteel.nativedb.core.SQLConverter.toSQL;
import static com.boosteel.nativedb.core.SQLConverter.toSQL0;

public class SQL {


    /*
     *  Map이 가지고 있는 properties를 이용해 SQL을 작성한다.
     *  Map의 value값이 가진 Java타입에 따라 SQL 데이터타입이 결정된다.
     */
    public static final String insert(IAccess iAccess, TableInfo info) {

        Set<String> keySet = iAccess.keySet();

        List<String>
                columns = new ArrayList<>(),
                values = new ArrayList<>();

        Set<String> props = info.props();


        for (String key : keySet) {
            if (props.contains(key)) {
                columns.add(key);
                values.add(toSQL0(info.getType(key), iAccess.get(key)));
            }

        }
        return new StringBuffer("(").append(String.join(", ", columns)).append(") VALUES (")
                .append(String.join(", ", values)).append(")")

                .toString();
    }

    public static final String mapToSQL(String sql, Object... values) {
        Map<String, Object> map = new HashMap<>();
        int l = values.length;
        while(l-- > 0) map.put(String.valueOf(l), values[l]);
        return mapToSQL(sql, map);
    }

    // "SELECT * FROM secret_gallery2 WHERE path = {0:int} AND title = {1}"
    public static final String mapToSQL(String sql, Map<String, Object> map) {
        Matcher matcher = Pattern.compile("\\{([^:]+?)(:.*?)?\\}").matcher(sql);
        StringBuffer sb = new StringBuffer();
        String cType;
        Object value;
        while (matcher.find()) {
            value = map.get(matcher.group(1));

            if(value == null)
                throw new RuntimeException("\"" + matcher.group(1) + "\" has not value.");

            if ((cType = matcher.group(2)) != null) {
                matcher.appendReplacement(sb, toQuery(cType.substring(1), value));
            }
            else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(SQLConverter.toSQL(value)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static final String insert(String tableName, Map<String, Object> value) {
        List<Map<String, Object>> values = new ArrayList<>();
        values.add(value);
        return insert(tableName, values);
    }

    public static final String insert(String tableName, List<Map<String, Object>> values) {
        Set<String> keys = values.get(0).keySet();
        StringBuffer buf = new StringBuffer("INSERT INTO ")
                .append(tableName)
                .append(" (")
                .append(columns(keys))
                .append(") values ");
        return values(keys, values, buf).toString();
    }


    /*
     *   WHERE id = {val} 로만 업데이트 할때 쓴다.
     *   value는 반드시 id를 가지고 있어야 한다.
     */
    public static final String update(IAccess<?> value, TableInfo info) {

        StringBuffer buf = new StringBuffer();
        String id = info.primaryKey.name;
        Set<String> props = info.props();

        for (String key : value.keySet()) {
            if (!key.equals(id) && props.contains(key))
                buf.append(key).append(" = ").append(toSQL0(info.getType(key), value.get(key))).append(", ");
        }

        return buf.delete(buf.length() - 2, buf.length()).toString();
    }


    // ****************

    public static final String selectPrefix(String str, String prefix) {
        prefix = prefix + ".";
        return prefix + str.replaceAll("\\s+", ", " + prefix);
    }


    // 컬럼명 기재
    public static StringBuffer columns(Iterable<String> keys) {
        return columns(keys, new StringBuffer());
    }

    public static StringBuffer columns(Iterable<String> keys, StringBuffer buffer) {
        return buffer.append(String.join(", ", keys));
    }


    // values 이하 작성
    public static StringBuffer values(Map<String, Object> values) {
        return values(values.keySet(), values, new StringBuffer());
    }

    public static StringBuffer values(Set<String> keys, Map<String, Object> values) {
        return values(keys, values, new StringBuffer());
    }

    public static StringBuffer values(Map<String, Object> values, StringBuffer buffer) {
        return values(values.keySet(), values, buffer);
    }

    public static StringBuffer values(Set<String> keys, Map<String, Object> values, StringBuffer buffer) {
        buffer.append("(");
        for (String key : keys) {
            buffer.append(toSQL(values.get(key))).append(",");
        }
        buffer.delete(buffer.length() - 1, buffer.length()).append(")");
        return buffer;
    }

    // 여러개
    public static StringBuffer values(Set<String> keys, List<Map<String, Object>> values, StringBuffer buffer) {
        for (Map<String, Object> v : values)
            values(keys, v, buffer).append(", ");
        return buffer.delete(buffer.length() - 2, buffer.length());
    }


    public interface ReduceHandler<T> {
        void accept(T t, ResultSet rs, int index) throws Exception;
    }

    public interface ReduceHandlerR<T> {
        T accept(T t, ResultSet rs, int index) throws Exception;
    }

    public interface CONVERT_HANDLER {
        Object convert(ResultSet rs, String dataType, int index) throws Exception;
    }


}
