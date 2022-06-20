package com.boosteel;


import com.boosteel.nativedb.NativeDB;
import com.boosteel.nativedb.core.SQL;
import com.boosteel.nativedb.core.SQLConverter;
import com.boosteel.nativedb.core.TableInfo;
import com.boosteel.util.support.MapAccess;
import com.boosteel.util.support.ReflectMap;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnyTest {


    @Test
    public void run() throws Exception {

        String sql = "SELECT * FROM secret_gallery2 WHERE path = {0:in}";

        Object[] s = new Object[]{"12", "qwe", "qwe", "Qwe"};

        out("뭐지");
        out(SQL.mapToSQL(sql, Arrays.asList(new Object[]{"12", 1, "qwe", "Qwe"})));

    }

    public void to(String s) {



    }

    public static final String num_SQL(String sql, Object... values) {
        Matcher matcher = Pattern.compile("\\{(\\d+)(:.*?)?\\}").matcher(sql);
        StringBuffer sb = new StringBuffer();
        String cType;
        Object value;
        while (matcher.find()) {
            value = values[Integer.parseInt(matcher.group(1))];
            if ((cType = matcher.group(2)) != null) {
                matcher.appendReplacement(sb, SQLConverter.toSQL0(cType.substring(1), value));
            }
            else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(SQLConverter.toSQL(value)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void cc(C u) {

    }

    interface C {
        Object get(Object key);

        Object put(String key, Object value);
    }


    public static void out(Object obj) {
        System.out.println(obj);
    }
}
