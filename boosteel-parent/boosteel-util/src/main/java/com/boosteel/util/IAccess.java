package com.boosteel.util;

import java.util.Set;

public interface IAccess<T> {
    T target();
    <T> T get(String key);
    IAccess<T> put(String key, Object value);
    Set<String> keySet();
    boolean isEmpty();
    boolean containsKey(Object key);
}
