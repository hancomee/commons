package com.boosteel.nativedb.core;

import com.boosteel.nativedb.NativeDB;

import java.util.*;

public class TableInfo implements Iterable<TableInfo.ColumnInfo> {

    private String name;
    public ColumnInfo primaryKey;

    private Set<String> set;
    private Map<String, ColumnInfo> columns;

    private TableInfo(String name) {
        this.name = name;
    }

    public Set<String> props() {
        return set;
    }

    public String getType(String key) {
        return columns.get(key).type;
    }

    public ColumnInfo get(String key) {
        ColumnInfo info = columns.get(key);
        if(info == null && primaryKey.name.equals(key)) {
            info = primaryKey;
        }
        return info;
    }

    public boolean hasColumn(String key) {
        return set.contains(key) ? true : primaryKey.name.equals(key);
    }

    public Set<String> getNames() {
        return set;
    }

    public Map<String, ColumnInfo> getColumns() {
        return columns;
    }

    @Override
    public Iterator<ColumnInfo> iterator() {
        List<ColumnInfo> infos = new ArrayList<>();
        infos.add(primaryKey);
        infos.addAll(columns.values());
        return infos.iterator();
    }

    private static final Map<String, TableInfo> INFO_CACHE = new HashMap<>();

    public static final TableInfo create(String name, NativeDB db) {

        TableInfo v = INFO_CACHE.get(name);
        if (v == null)

            // "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='" + name + "' GROUP BY COLUMN_NAME"
            v = db.execute("DESC " + name,
                    (rs) -> {
                        TableInfo info = new TableInfo(name);
                        Map<String, ColumnInfo> columns = info.columns = new HashMap<>();
                        ColumnInfo n;
                        String[] fields;
                        while (rs.next()) {
                            n = new ColumnInfo(
                                    rs.getString("Field"),
                                    (fields = fieldName(rs.getString("Type")))[0].toUpperCase(),
                                    fields[1] == null ? -1 : Integer.parseInt(fields[1]),
                                    rs.getString("Null").equals("NO")
                            );

                            columns.put(n.name, n);

                            if (rs.getString("Key").equals("PRI"))
                                info.primaryKey = n;

                        }
                        info.set = columns.keySet();

                        if (columns.isEmpty())
                            throw new RuntimeException(name + "은 존재하지 않는 테이블입니다.");

                        INFO_CACHE.put(name, info);
                        return info;
                    });

        return v;
    }

    private static String[] fieldName(String field) {
        String size = null;
        int p = field.indexOf("(");
        if (p != -1) {
            size = field.substring(p + 1, field.length() - 1);
            field = field.substring(0, p);
        }
        return new String[]{field, size};
    }

    public static class ColumnInfo {
        public String name;
        public String type;
        public int size;
        public boolean notNull;

        public ColumnInfo(String n, String t, int s, boolean no) {
            name = n;
            type = t;
            size = s;
            notNull = no;
        }

        @Override
        public String toString() {
            return (notNull ? "[notNull]" : "[Nullable]") + " " +
                    name + " / " + type + (size == -1 ? "" : "(" + size + ")");

        }
    }
}
