package com.boosteel.nativedb.core;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLConverter {

    //********************************* ▼ JAVA => SQL ▼ *********************************//
    /*
     *  VALUES ('data', 0)...
     *  SQL 데이터타입을 알고 있을때, 그에 알맞게 문자열 변환
     */
    public static String toSQL(String sqlType, String value) {

        switch (sqlType) {
            case "TIMESTAMP":
            case "DATETIME":
            case "DATE":
            case "TIME":
            case "YEAR":
                if (value.matches("\\d+")) {
                    value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(value)));
                    if (sqlType.equals("YEAR")) value = value.substring(0, value.indexOf("-"));
                    else if (sqlType.equals("TIME")) value = value.split(" ")[1];
                    else if (sqlType.equals("DATE")) value = value.split(" ")[0];
                }
                return "'" + value + "'";
            case "CHAR":
            case "VARCHAR":
            case "TEXT":
                return "'" + value.replaceAll("('|\\\\)", "\\\\$1") + "'";
            case "BOOL":
            case "BOOLEAN":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "INTEGER":
            case "BIGINT":
            case "BIT":
            case "DECIMAL":
            case "DEC":
            case "NUMERIC":
            case "FIXED":
            case "DOUBLE":
            case "DOUBLE PRECISION":
            case "FLOAT":
                if (value.isEmpty()) return "0";
            default:
                return value;
        }
    }


    /*
     *  @INSERT, @UPDATE 용
     */
    public static String toSQL0(String type, Object value) {
        return value == null ? "null" : toSQL(type, value.toString());
        /*if (value instanceof String) return toSQL(type, value.toString());
        return toSQL(value);*/

    }

    //********************************* ▼ SQL => JAVA ▼ *********************************//
    /*
     *  DB reading시 Map<String, Object>에 들어갈때 아래 메서드를 통해 변환한다.
     */
    // DB 데이터타입에 맞춰 데이터 변환하기
    public static Object bySQL(ResultSet rs, String sqlType, int index) throws Exception {
        switch (sqlType) {
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "INTEGER":
                return rs.getInt(index);
            case "BIT":
            case "BOOL":
            case "BOOLEAN":
                return rs.getBoolean(index);
            case "DECIMAL":
            case "DEC":
            case "NUMERIC":
            case "FIXED":
            case "DOUBLE":
            case "DOUBLE PRECISION":
            case "FLOAT":
            case "BIGINT":
                return rs.getLong(index);

            //  JSON 변환을 위해 날짜타입은 숫자형으로 내보낸다.
            case "TIMESTAMP":
            case "DATETIME":
            case "DATE":
            case "TIME":
            case "YEAR":
                Timestamp stamp = rs.getTimestamp(index);
                return stamp == null ? null : stamp.getTime();
            default:
                return rs.getString(index);
        }
    }


    /*
     * JSON의 날짜를 숫자로 보내주기 위해 추가한 메서드
     * 데이터타입이 날짜인 경우만 숫자로 변경해준다.
     */
    /*public static Object bySQLtoJSON(ResultSet rs, String dataType, int index) throws Exception {
        switch (dataType) {
            case "TIMESTAMP":
            case "DATETIME":
            case "DATE":
            case "TIME":
            case "YEAR":
                Timestamp stamp = rs.getTimestamp(index);
                return stamp == null ? null : stamp.getTime();
            default:
                return bySQL(rs, dataType, index);
        }
    }*/


    /*
     *
     * Repository에서 반환 타입(List<...type>)에 맞춰 값을 내보내기 위해 사용하는 메서드
     * 제네릭 타입에서 추출한 타입정보라 문자열이다.
     * :: method.getGenericReturnType().toString()
     *
     */
    public static Object byJAVA(ResultSet rs, String javaType, int index) throws Exception {
        switch (javaType) {
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
                return rs.getInt(index);

            case "java.util.Date":
                Timestamp t = rs.getTimestamp(index);
                return t == null ? null : new Date(t.getTime());
            case "java.lang.Boolean":
            case "boolean":
                return rs.getBoolean(index);
            case "java.lang.String":
                return rs.getString(index);
            default:
                return rs.getObject(index);
        }
    }

    /*
     * 자바 타입을 참조해 알맞는 SQL 문자열 값으로 변환해준다.
     * TableInfo가 없는 환경에서 사용한다.
     */
    public static String toSQL(Object val) {
        if (val == null) return "null";

        Class<?> clazz = val.getClass();
        if (CharSequence.class.isAssignableFrom(clazz))
            return "'" + val.toString().replaceAll("('|\\\\)", "\\\\$1") + "'";

        if (Date.class.isAssignableFrom(clazz))
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) val) + "'";

        return val.toString();
    }







}
