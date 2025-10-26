package javasabr.mqtt.base.util;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javasabr.rlib.common.util.StringUtils;
import reactor.core.publisher.Mono;

public class ReactorUtils {

  public static Function<Boolean, Boolean> ifTrue(Runnable function) {
    return value -> {
      if (value) {
        function.run();
      }
      return value;
    };
  }

  public static <A> Function<Boolean, Boolean> ifTrue(A arg, Consumer<A> function) {
    return value -> {
      if (value) {
        function.accept(arg);
      }
      return value;
    };
  }

  public static <R> Function<String, Mono<R>> ifNotEmpty(
      Function<String, Mono<R>> toContinue,
      Supplier<Mono<R>> another) {
    return value -> {
      if (StringUtils.isNotEmpty(value)) {
        return toContinue.apply(value);
      } else {
        return another.get();
      }
    };
  }

  public static <R> Function<Boolean, Mono<R>> ifTrue(Supplier<Mono<R>> function, Runnable another) {
    return value -> {

      if (!value) {
        another.run();
        return Mono.empty();
      }

      return function.get();
    };
  }

  public static <R, T1, T2, T3> Function<Boolean, Mono<R>> ifTrue(
      T1 arg1,
      T2 arg2,
      BiFunction<T1, T2, Mono<R>> function,
      T3 arg3,
      Consumer<T3> another) {
    return value -> {

      if (value) {
        return function.apply(arg1, arg2);
      }

      another.accept(arg3);
      return Mono.empty();
    };
  }
}
