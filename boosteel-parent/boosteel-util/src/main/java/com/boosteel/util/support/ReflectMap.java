package com.boosteel.util.support;

import com.boosteel.util.IAccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReflectMap {


    /*
     *  모든 BeanData는 캐시에 저장된다.
     *  Class.getName()은 유일한 값이므로 getName()으로 캐시에 저장한다.
     */
    private static final Map<Class<?>, ReflectMap> $CACHE = new HashMap<>();

    /*public static final ReflectMap get(Class<?> clazz) {
        return get(clazz.getName());
    }*/

    public static final ReflectMap get(Class<?> clazz) {
        ReflectMap map = $CACHE.get(clazz);
        if (map == null) {
            try {
                $CACHE.put(clazz, map = new ReflectMap(clazz));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    // reflect로 값 읽어오기 :: dot 지원
    public static final <T> T read(Object target, String key) {
        String[] keys = key.split("\\.");
        ReflectMap map;

        int len = keys.length, i = 0;

        for (; i < len; i++) {
            if (target == null) return null;
            map = ReflectMap.get(target.getClass());
            target = map.get0(target, keys[i]);
        }
        return (T) target;
    }

    public static final <T> void write(T target, String key, Object value) {
        try {
            write0(target, key.split("\\."), value, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final <T> T write0(T target, String[] v, Object value, int index) throws Exception {

        ReflectMap map = get(target.getClass());
        Class<?> clazz = map.setterTypes.get(v[index]);

        if (clazz != null) {
            // 실제 값 입력
            if (v.length - 1 == index) {
                map.set0(target, v[index], value);
            }
            // 하위 객체
            else {
                Object t = map.get0(target, v[index]);
                if (t == null) {
                    t = get(map.setterTypes.get(v[index])).cons.newInstance();
                }
                map.set0(target, v[index], write0(t, v, value, index + 1));
            }
        }
        return target;
    }


    /*
     *  get / set 으로 데이터를 입출력할 수 있게 해준다.
     *  Map과 Class를 하나의 인터페이스로 쓸 수 있게 하기 위해 만든 로직
     */
    public static final <T> IAccess<T> createAccess(Class<T> clazz) {
        try {
            return createAccess(clazz.getConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final <T> IAccess<T> createAccess(T t) {
        Map<String, Class<?>> types = get(t.getClass()).setterTypes;
        return new IAccess<T>() {
            public T target() {
                return t;
            }

            public Object get(String key) {
                return read(t, key);
            }

            public IAccess<T> put(String key, Object value) {
                write(t, key, value);
                return this;
            }

            public boolean isEmpty() {
                return types.isEmpty();
            }

            public boolean containsKey(Object key) {
                return types.containsKey(key);
            }

            public Set<String> keySet() {
                return types.keySet();
            }
        };
    }

    private static final String keyName(String n) {
        n = n.substring(3);
        return n.substring(0, 1).toLowerCase() + n.substring(1);
    }


    /* **************************** Class **************************** */
    protected Constructor<?> cons;
    protected Class<?> clazz;
    protected Map<String, Setter> setterMap = new HashMap<>();
    protected Map<String, Getter> getterMap = new HashMap<>();
    protected Map<String, Class<?>> setterTypes = new HashMap<>();       // setter가 가진 클래스

    ReflectMap(Class<?> clazz) {
        this.clazz = clazz;
        try {

            cons = clazz.getConstructor();
            Method[] methods = clazz.getMethods();
            Field[] fields = clazz.getFields();

            String name;
            int paramCount;

            /*
             *  set, get 메서드가 있을 경우 그게 우선이고,
             *  없으면 field를 직접 사용한다.
             */
            for (Method method : methods) {

                name = method.getName();
                paramCount = method.getParameterCount();

                // setter
                if (name.startsWith("set")) {

                    // void setName(Object obj);
                    if (paramCount == 1) {
                        method.setAccessible(true);
                        Method invoke = method;

                        Class<?> c = method.getParameterTypes()[0];
                        name = keyName(name);
                        setterTypes.put(name, c);
                        setterMap.put(name, (t, v) -> invoke.invoke(t, v));
                    }

                }

                // getter
                else if (name.startsWith("get")) {
                    if (!method.getReturnType().equals(Void.TYPE) && paramCount == 0) {
                        method.setAccessible(true);
                        Method invoke = method;
                        name = keyName(name);
                        getterMap.put(name, (t) -> invoke.invoke(t));
                    }
                }
            }


            for (Field field : fields) {
                name = field.getName();
                field.setAccessible(true);

                if (!getterMap.containsKey(name))
                    getterMap.put(name, (t) -> field.get(t));

                if (!setterMap.containsKey(name)) {
                    setterTypes.put(name, field.getType());
                    setterMap.put(name, (t, v) -> field.set(t, v));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 값 가지고 오기
    private <T> T get0(Object obj, String key) {
        Getter g = getterMap.get(key);
        if (g != null) {
            try {
                return (T) g.get(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private void set0(Object obj, String name, Object val) throws Exception {
        Setter setter = setterMap.get(name);
        if (setter != null)
            try {
                setter.set(obj, val);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }


    public Map<String, Object> map(Object target) throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Getter> entry : getterMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get(target));
        }
        return map;
    }

    public interface Setter {
        void set(Object target, Object value) throws Exception;
    }

    public interface Getter {
        Object get(Object target) throws Exception;
    }
}
