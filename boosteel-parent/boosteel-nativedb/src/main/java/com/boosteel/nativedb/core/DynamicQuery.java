package com.boosteel.nativedb.core;

import com.boosteel.util.IAccess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicQuery {

    /*
     * {변수}가 포함된 SQL 구문을 파싱해,
     * SQL을 완성시키는 하나의 함수를 반환한다.
     */
    public static DynamicQueryHandler dynamicQuery(String lines) {
        List<Function<IAccess<?>, String>> list = parser0(lines);
        return (map) -> {
            String[] values = new String[list.size()];
            int i = 0;
            for (Function<IAccess<?>, String> func : list) values[i++] = func.apply(map);
            return String.join("", values);
        };
    }

    private static List<Function<IAccess<?>, String>> parser0(String lines) {

        List<Function<IAccess<?>, String>> list = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\{)|(\\s+\\[)");
        Matcher m = pattern.matcher(lines);

        String line;
        int cursor = 0, pos;

        while (m.find(cursor)) {
            pos = m.start();

            // 단순 문자열
            if (cursor != pos) {
                String text = lines.substring(cursor, pos);
                list.add((o) -> text);
            }

            if (m.group(1) != null) {
                line = lines.substring(pos + 1, pos = lines.indexOf("}", pos));
                list.add(__value(line));
            } else {
                line = lines.substring(pos, pos = lines.indexOf("]", pos)).replaceFirst("\\[", "");
                list.add(__line(line));
            }
            cursor = pos + 1;
        }

        // 남은 문자열은 단순 텍스트로..
        if (lines.length() > cursor) {
            String text = lines.substring(cursor);
            list.add((o) -> text);
        }

        return list;
    }


    // [AND this.title LIKE {title:%_%}]
    // [INNER JOIN hancomee_workitem item ON work.id = item.work_id   {?itemSubject,print}]
    private static Function<IAccess<?>, String> __line(String sql) {
        List<Function<IAccess<?>, String>> list = parser0(sql);
        return (map) -> {
            String[] values = new String[list.size()];
            int i = 0;
            // 값 중 하나라도 null이면 구문을 삭제한다.
            for (Function<IAccess<?>, String> func : list) {
                if ((values[i++] = func.apply(map)) == null) return "";
            }
            return String.join("", values);
        };
    }

    // {title:%_%}
    // SQL에 표기된 값은 무조건 map에 값이 들어있다는 전제하에 동작한다.
    // 없을 경우) "prop = null"
    private static Function<IAccess<?>, String> __value(String sql) {
        if (sql.startsWith("?")) {
            // {?prop}, {?prop1,prop2}
            if (sql.startsWith("?")) {
                String[] values = sql.substring(1).split(",");
                return (map) -> {
                    for (String val : values)
                        if (map.get(val) == null) return null;
                    return "";
                };
            }
        }

        String[] values = sql.split(":");
        String prop = values[0],
                type = values.length == 1 ? "string" : values[1];

        return (map) -> toQuery(type, map.get(prop));

    }

    public static String toQuery(String type, Object value) {
        if (value == null) return null;
        String v;
        switch (type) {
            case "bool":
                if (value == null) return "0";
                if (value instanceof Number) {
                    long l = ((Number) value).longValue();
                    return l == 0 ? "0" : "1";
                }
                if (value instanceof String) {
                    v = value.toString();
                    return v.isEmpty() || v.equals("0") ? "0" : "1";
                }
                return "1";
            case "int":
            case "i":
                return value.toString();
            case "d":
            case "date":
                return __quotes(__dateformat(value, "yyyy-MM-dd HH:mm:ss"));
            case "%_":
                return __quotes("%" + __replace(value));
            case "_%":
                return __quotes(__replace(value) + "%");
            case "%_%":
            case "%":
                return __quotes("%" + __replace(value) + "%");
            case "st":
                v = __dateformat(value, "yyyy-MM-dd");
                return __quotes(v + " 00:00:00.000");
            case "et":
                v = __dateformat(value, "yyyy-MM-dd");
                return __quotes(v + " 23:59:59.999");
            default:

                // where IN 쿼리  List, Set만 받을 수 있다.
                if (type.startsWith("...")) {

                    Iterable<?> col = (Iterable<?>) value;
                    List<String> values = new ArrayList<>();
                    type = type.length() == 3 ? "string" : type.substring(3);

                    for (Object obj : col) {
                        values.add(toQuery(type, obj));
                    }
                    return "(" + String.join(", ", values) + ")";
                }
                return __quotes(__replace(value));

        }
    }


    private static String __dateformat(Object value, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (value instanceof Number)
            return sdf.format(((Number) value).longValue());
        if (value instanceof Date)
            return sdf.format((Date) value);
        return value.toString();
    }

    private static String __replace(Object s) {
        return s.toString().replaceAll("(%|'|\\\\)", "\\\\$1");
    }

    // % ' \ 는 이스케이프 문자를 추가해준다
    private static String __quotes(String s) {
        return "'" + s + "'";
    }

}
