package com.boosteel.nativedb.core;

import com.boosteel.util.IAccess;

import java.util.function.Function;

public interface DynamicQueryHandler extends Function<IAccess<?>, Object> {
    String apply(IAccess<?> map);
}
