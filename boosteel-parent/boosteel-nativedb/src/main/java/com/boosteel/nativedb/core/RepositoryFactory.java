package com.boosteel.nativedb.core;

import com.boosteel.nativedb.NativeDB;
import com.boosteel.nativedb.core.anno.*;
import com.boosteel.nativedb.core.support.Pager;
import com.boosteel.nativedb.core.support.RepositoryConfig;
import com.boosteel.util.IAccess;
import com.boosteel.util.support.MapAccess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

import static com.boosteel.nativedb.core.DynamicQuery.dynamicQuery;
import static com.boosteel.nativedb.core.SQLConverter.byJAVA;

public class RepositoryFactory {

    // @Value용 객체
    private static class QVal {
        int index;
        String key;

        QVal(int i, String k) {
            index = i;
            key = k;
        }
    }

    private static class MethodData {

        NativeDB db;
        int pagerIndex = -1;
        int statementIndex = -1;
        int connectionIndex = -1;
        int mapIndex = -1;

        List<QVal> values = new ArrayList<>();

        /*
         *  Method의 모든 정보를 뽑아낸다.
         *
         *  리턴타입
         *  ① List<Map<String, Object>>
         *  ② Map<String, Object>
         *  ③ int
         *  ④ long
         *  ⑤ String
         *  ⑥ java.util.Date
         *  ⑦ Pager
         *
         *
         *  파라미터로 허용되는건
         *  ① @Value
         *  ③ Map<String, Object>
         *  ④ Connection
         *  ⑤ Statement
         *  ⑥ Pager
         */
        MethodData(Method method, NativeDB db) throws Exception {


            this.db = db;

            int i = 0;
            Class<?>[] parameters = method.getParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();

            /*
             *  메소드 파라미터 순회
             */
            for (Class<?> param : parameters) {

                // @Value
                if (annotations[i].length == 1 && Value.class.isAssignableFrom(annotations[i][0].getClass())) {
                    values.add(new QVal(i, ((Value) annotations[i][0]).value()));
                }
                // Pager
                else if (Pager.class.isAssignableFrom(param)) {
                    pagerIndex = i;
                }
                // Map
                else if (Map.class.isAssignableFrom(param)) {
                    mapIndex = i;
                }
                // Connection
                else if (Connection.class.isAssignableFrom(param)) {
                    connectionIndex = i;
                }
                // Statement
                else if (Statement.class.isAssignableFrom(param)) {
                    statementIndex = i;
                }
                // error
                else {
                    throw new RuntimeException(param + " 은 지원하지 않는 Parameter입니다.");
                }
                i++;
            }
        }


        Object doStmt(Object[] args, DoStmt r) throws Exception {
            // Statement가 인자로 제공될때
            if (statementIndex != -1) {
                return r.run((Statement) args[statementIndex]);
            }
            // Connection이 인자로 제공될때
            else if (connectionIndex != -1) {
                try (Statement stmt = ((Connection) args[connectionIndex]).createStatement()) {
                    return r.run(stmt);
                }
            }
            // 아무것도 없을때
            else {
                return db.doStmtR(stmt -> r.run(stmt));
            }
        }

        // 파라미터로 들어온 값들을 하나의 Map에 담는다
        IAccess<?> getAccess(Object[] args) {
            Map<String, Object> map = mapIndex != -1 ?
                    new HashMap<>((Map<String, Object>) args[mapIndex]) :
                    new HashMap<>();
            for (QVal v : values) map.put(v.key, args[v.index]);
            return new MapAccess(map);
        }
    }


    public interface DoStmt {
        Object run(Statement stmt) throws Exception;
    }

    public interface MethodRun {
        Object run(Object[] args) throws Exception;

    }

    private static final <T> T _log(T t) {
        System.out.println("\n-----------------------------------------------------------------\n" +
                t + "\n-----------------------------------------------------------------\n");
        return t;
    }


    public static final <T> T createRepository(Class<T> cons, NativeDB db, RepositoryConfig config) {

        try {

            Map<Method, MethodRun> result = new HashMap<>();
            Function<String, String> $sql = (String s) -> {
                if (s.startsWith("#")) {
                    String v = config.getSQL(s.substring(1));
                    if (v == null) throw new RuntimeException(s + "에 해당하는 sql이 없습니다.!!");
                    return v;
                }
                return s;
            };

            for (Method method : cons.getMethods()) {

                Class<?> returnType = method.getReturnType();
                MethodData md = new MethodData(method, db);

                Insert INSERT;
                Update UPDATE;
                Selector SELECTOR;
                Save SAVE;
                PageList PAGE_LIST;
                SQLString SQL_STRING;

                // @Save
                if ((SAVE = method.getAnnotation(Save.class)) != null) {
                    boolean wantLastId = SAVE.lastId();
                    DynamicQueryHandler query = dynamicQuery($sql.apply(SAVE.value()));

                    result.put(method, args ->
                            md.doStmt(args, stmt -> {
                                int i = stmt.executeUpdate(_log(query.apply(md.getAccess(args))));
                                if (wantLastId) {
                                    try (ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                                        rs.next();
                                        i = rs.getInt(1);
                                    }
                                }
                                return i;
                            }));
                }
                /*
                 * ★★★★★★★★★★★★
                 * Update와 Insert에서 사용하는 TableInfo는 첫 로딩시에 한번만 불러온다.
                 * 따라서 런타임 실행 중에 DB 테이블을 변경하면 심각한 문제를 초래할 수 있다.
                 * 반드시 기억하자.
                 */
                // @Update
                else if ((UPDATE = method.getAnnotation(Update.class)) != null) {

                    String tableName = UPDATE.value(),
                            sql = "UPDATE " + tableName + " SET ";

                    DynamicQueryHandler where = dynamicQuery(method.getAnnotation(Update.class).where());
                    TableInfo info = db.getTableInfo(tableName);

                    result.put(method, args ->
                            md.doStmt(args, stmt -> {
                                IAccess access = md.getAccess(args);
                                return stmt.executeUpdate(_log(
                                        sql + SQL.update(access, info) + " WHERE " +
                                                where.apply(access)));
                            }));
                }

                // @Insert
                else if ((INSERT = method.getAnnotation(Insert.class)) != null) {
                    boolean wantLastId = INSERT.lastId();
                    String tableName = INSERT.value(),
                            sql = "INSERT INTO " + tableName + " ";
                    TableInfo info = db.getTableInfo(tableName);

                    result.put(method, args -> md.doStmt(args, stmt -> {
                        int i = stmt.executeUpdate(_log(sql + SQL.insert(md.getAccess(args), info)));
                        if (wantLastId) {
                            try (ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                                rs.next();
                                i = rs.getInt(1);
                            }
                        }
                        return i;
                    }));
                }

                // @SQLString
                else if ((SQL_STRING = method.getAnnotation(SQLString.class)) != null) {

                    if (!String.class.isAssignableFrom(returnType))
                        throw new RuntimeException("@SQLString error : \n" +
                                "반환값은 반드시 java.lang.String이어야 합니다.");

                    DynamicQueryHandler query = dynamicQuery($sql.apply(SQL_STRING.value()));
                    result.put(method, (args) -> query.apply(md.getAccess(args)));
                }

                // @PageList
                else if ((PAGE_LIST = method.getAnnotation(PageList.class)) != null) {


                    if (!Pager.class.isAssignableFrom(returnType))
                        throw new RuntimeException("@PageList error : \n" +
                                "리턴값은 반드시 Pager객체이어야 합니다.");
                    if (md.pagerIndex == -1)
                        throw new RuntimeException("@PageList error : \n" +
                                "Pager객체가 반드시 인자로 제공되어야 합니다.");

                    DynamicQueryHandler $select = dynamicQuery($sql.apply(PAGE_LIST.list())),
                            $count = dynamicQuery($sql.apply(PAGE_LIST.count()));

                    result.put(method, (args) -> md.doStmt(args, stmt -> {
                        Pager pager = (Pager) args[md.pagerIndex];
                        IAccess access = md.getAccess(args);
                        String select = _log($select.apply(access) + pager.orderBy() + pager.limit()),
                                count = _log($count.apply(access));

                        pager.setContents(DataAccess.readAll(stmt.executeQuery(select)));
                        ResultSet rs = stmt.executeQuery(count);
                        rs.next();
                        return pager.setTotalElements(rs.getLong(1));
                    }));
                }

                // @Selector
                else if ((SELECTOR = method.getAnnotation(Selector.class)) != null) {

                    String type = returnType.getName();

                    DynamicQueryHandler query = dynamicQuery($sql.apply(SELECTOR.value()));

                    // return ResultSet
                    if (ResultSet.class.isAssignableFrom(returnType)) {
                        result.put(method, args ->
                                md.doStmt(args, stmt -> stmt.executeQuery(_log(query.apply(md.getAccess(args))))));
                    }

                    // return Map<String, Object>
                    else if (Map.class.isAssignableFrom(returnType)) {

                        result.put(method, (args) ->
                                md.doStmt(args, stmt -> {
                                    try (ResultSet rs = stmt.executeQuery(_log(query.apply(md.getAccess(args))))) {
                                        if (rs.next())
                                            return DataAccess.read(rs);
                                        else
                                            return new HashMap<>();
                                    }
                                })
                        );

                    }


                    // return List<?> ▼▼▼

                    // return List<Map<String, Object>>
                    else if (List.class.isAssignableFrom(returnType)) {

                        String gType = method.getGenericReturnType().toString();
                        gType = gType.substring(gType.indexOf("<") + 1, gType.lastIndexOf(">"));

                        // List<Map<String, Object>>
                        if (gType.startsWith("java.util.Map")) {
                            result.put(method, (args) ->
                                    md.doStmt(args, stmt -> {
                                        try (ResultSet rs = stmt.executeQuery(_log(query.apply(md.getAccess(args))))) {
                                            return DataAccess.readAll(rs);
                                        }
                                    })
                            );
                        }

                        // List<java.lang.??>
                        else if (gType.startsWith("java.lang.")) {

                            String TYPE = gType.substring("java.lang.".length());

                            result.put(method, (args) ->
                                    md.doStmt(args, stmt -> {
                                        try (ResultSet rs = stmt.executeQuery(_log(query.apply(md.getAccess(args))))) {
                                            List list = new ArrayList();
                                            while (rs.next()) {
                                                switch (TYPE) {
                                                    case "Integer":
                                                        list.add(rs.getInt(1));
                                                        break;
                                                    case "Long":
                                                        list.add(rs.getLong(1));
                                                        break;
                                                    case "String":
                                                        list.add(rs.getString(1));
                                                        break;
                                                    case "Boolean":
                                                        list.add(rs.getBoolean(1));
                                                        break;
                                                    default:
                                                        list.add(rs.getObject(1));
                                                }
                                            }
                                            return list;
                                        }
                                    })
                            );
                        }

                        // List<java.util.Date>
                        else if (gType.equals("java.util.Date")) {
                            result.put(method, (args) ->
                                    md.doStmt(args, stmt -> {
                                        try (ResultSet rs = stmt.executeQuery(_log(query.apply(md.getAccess(args))))) {
                                            List list = new ArrayList();
                                            while (rs.next())
                                                list.add(new Date(rs.getTimestamp(1).getTime()));
                                            return list;
                                        }
                                    })
                            );
                        } else {
                            throw new RuntimeException("@Selector error : \n" +
                                    method.getGenericReturnType() + " 은 지원하지 않는 반환값입니다.");
                        }

                    }
                    // return JAVA DEFAULT TYPE
                    else if (type.indexOf(".") == -1 || type.startsWith("java.lang")) {
                        result.put(method, (args) -> md.doStmt(args, stmt -> {
                            try (ResultSet rs = stmt.executeQuery(_log(query.apply(md.getAccess(args))))) {
                                if (rs.next()) return byJAVA(rs, type, 1);
                                else return null;
                            }
                        }));
                    }

                    // error
                    else {
                        throw new RuntimeException("@Selector error : \n" +
                                type + " 은 지원하지 않는 반환값입니다.");
                    }
                }
                // error
                else {
                    throw new RuntimeException(method + " 시그니처를 확인하세요.");
                }
            }


            return (T) Proxy.newProxyInstance(
                    cons.getClassLoader(),
                    new Class[]{cons},
                    (proxy, method, methodArgs) -> result.get(method).run(methodArgs));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
