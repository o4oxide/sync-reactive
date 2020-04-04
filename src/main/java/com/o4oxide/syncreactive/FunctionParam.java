package com.o4oxide.syncreactive;

import java.util.function.Function;

public final class FunctionParam<T, R> {

    private final Function<T, R> function;
    private final T input;

    private FunctionParam(Function<T, R> function, T input) {
        this.function = function;
        this.input = input;
    }

    public static <T,R> FunctionParam<T, R> of(Function<T, R> function, T input) {
        return new FunctionParam<>(function, input);
    }

    public R execute() {
        return function.apply(input);
    }
}
