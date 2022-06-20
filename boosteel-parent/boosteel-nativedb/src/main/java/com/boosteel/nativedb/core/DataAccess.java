package com.boosteel.nativedb.core;

import com.boosteel.util.support.MapAccess;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class DataAccess {

    /*
        특정 키값으로 가지고 오기
        {
          'key': [ {values..}, {values...} ]
        }
     */
    public static Map<String, List<Map<String, Object>>> read(ResultSet rs, String column) throws Exception {

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Meta> metaList = metaList(rs.getMetaData());

        String key;
        List<Map<String, Object>> list;

        while (rs.next()) {

            Map<String, Object> values = new HashMap<>();
            key = rs.getString(column);   // key는 string으로 가지고 온다.

            list = result.get(key);
            if (list == null) result.put(key, list = new ArrayList<>());

            for (Meta meta : metaList) {
                MapAccess.put(values, meta.key, SQLConverter.bySQL(rs, meta.type, meta.index));
            }
            list.add(values);

        }

        return result;
    }

    public static List<Map<String, Object>> readAll(ResultSet rs) throws Exception {
        List<Meta> metaMap = metaList(rs.getMetaData());
        List<Map<String, Object>> result = new ArrayList<>();
        while (rs.next()) result.add(read(rs, metaMap));
        return result;
    }

    public static Map<String, Object> read(ResultSet rs) throws Exception {
        return read(rs, metaList(rs.getMetaData()));
    }

    public static Map<String, Object> read(ResultSet rs, List<Meta> metaMap) throws Exception {
        MapAccess result = MapAccess.create(new HashMap<>());
        for (Meta meta : metaMap) {
            result.put(meta.key, SQLConverter.bySQL(rs, meta.type, meta.index));
        }
        return result.target();
    }

    // <[MapAccess.key, ColumnType, TableName]>
    public static List<Meta> metaList(ResultSetMetaData metaData) throws SQLException {
        List<Meta> result = new ArrayList<>();
        Meta meta;

        int i = 1, len = metaData.getColumnCount() + 1;
        String tableName, columnLabel, columnType;

        while (i < len) {
            tableName = metaData.getTableName(i);
            columnLabel = metaData.getColumnLabel(i);
            columnType = metaData.getColumnTypeName(i);

            meta = new Meta(
                    i, convertKey(tableName, columnLabel),
                    tableName, columnLabel, columnType
            );

            result.add(meta);
            i++;
        }
        return result;
    }


    public static Map<String, List<Meta>> metaMap(ResultSetMetaData metaData) throws SQLException {
        Map<String, List<Meta>> result = new HashMap<>();
        Meta meta;

        int i = 1, len = metaData.getColumnCount() + 1;
        String tableName, columnLabel, columnType;
        List<Meta> list;

        while (i < len) {
            tableName = metaData.getTableName(i);
            columnLabel = metaData.getColumnLabel(i);
            columnType = metaData.getColumnTypeName(i);

            meta = new Meta(
                    i, convertKey(tableName, columnLabel),
                    tableName, columnLabel, columnType
            );

            if ((list = result.get(tableName)) == null)
                result.put(tableName, list = new ArrayList<>());

            list.add(meta);
            i++;
        }
        return result;
    }


    public static class Meta {
        public int index;
        public String key;          // MapAccess에 저장될 키값
        public String table;        // 테이블 명
        public String label;        // column명
        public String type;         // 데이터타입

        public Meta(int index, String key, String table, String label, String type) {
            this.index = index;
            this.table = table;
            this.label = label;
            this.type = type;
            this.key = key;
        }
    }

    /*
     *  table           put("column", value)
     *  _table          put("table.column", value)
     *  table_sub       put("table.sub.column", value)
     */
    private static String convertKey(String key, String column) {
        if (!key.contains("_")) return column;
        key = key.replaceAll("_", ".");
        if (key.startsWith(".")) key = key.substring(1);
        return key + "." + column;
    }


}
