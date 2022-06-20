package com.boosteel;


import com.boosteel.nativedb.NativeDB;
import com.boosteel.nativedb.core.DynamicQuery;
import com.boosteel.nativedb.core.DynamicQueryHandler;
import com.boosteel.nativedb.core.TableInfo;
import com.boosteel.util.IAccess;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DBTest {


    @Test
    public void run() throws Exception {

        String sql = "SELECT * FROM table WHERE magic = :nul [AND name = :dld{i}] ORDER BY names DESC";
    }

    public void out(Object obj) {
        System.out.println(obj);
    }
}
