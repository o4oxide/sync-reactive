package com.o4oxide.syncreactive;

import java.util.function.Function;

public final class FunctionInstance<T, R> {

    private final Function<T, R> function;
    private final T input;

    private FunctionInstance(Function<T, R> function, T input) {
        this.function = function;
        this.input = input;
    }

    public static <T,R> FunctionInstance<T, R> of(Function<T, R> function, T input) {
        return new FunctionInstance<>(function, input);
    }

    public R execute() {
        return function.apply(input);
    }
}
