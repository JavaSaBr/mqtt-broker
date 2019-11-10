package com.ss.mqtt.broker.util;

import com.ss.rlib.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReactorUtils {

    public static @NotNull Function<Boolean, Boolean> ifTrue(@NotNull Runnable function) {
        return value -> {
            if (value) {
                function.run();
            }
            return value;
        };
    }

    public static <A> @NotNull Function<Boolean, Boolean> ifTrue(@NotNull A arg, @NotNull Consumer<A> function) {
        return value -> {
            if (value) {
                function.accept(arg);
            }
            return value;
        };
    }

    public static <R> @NotNull Function<String, Mono<R>> ifNotEmpty(
        @NotNull Function<String, Mono<R>> toContinue,
        @NotNull Supplier<Mono<R>> another
    ) {
        return value -> {
            if (StringUtils.isNotEmpty(value)) {
                return toContinue.apply(value);
            } else {
                return another.get();
            }
        };
    }

    public static <R> @NotNull Function<Boolean, Mono<R>> ifTrue(
        @NotNull Supplier<Mono<R>> function,
        @NotNull Runnable another
    ) {
        return value -> {

            if (!value) {
                another.run();
                return Mono.empty();
            }

            return function.get();
        };
    }

    public static <R, T1, T2, T3> @NotNull Function<Boolean, Mono<R>> ifTrue(
        @NotNull T1 arg1,
        @NotNull T2 arg2,
        @NotNull BiFunction<T1, T2, Mono<R>> function,
        @NotNull T3 arg3,
        @NotNull Consumer<T3> another
    ) {
        return value -> {

            if (value) {
                return function.apply(arg1, arg2);
            }

            another.accept(arg3);
            return Mono.empty();
        };
    }
}
