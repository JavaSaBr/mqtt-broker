package javasabr.mqtt.model;

import org.jspecify.annotations.Nullable;
import javasabr.rlib.common.util.NumberedEnum;

/**
 * Reflection-free replacement for {@code NumberedEnumMap}. Indexed by {@link NumberedEnum#number()}
 * using a plain {@code Object[]} allocation, so it adds no native-image reachability metadata the way
 * {@code Class#getEnumConstants()} / {@code Array#newInstance()} do. Callers pass {@code values()}
 * explicitly; the lookup table is sized to the maximum declared number plus one.
 */
public final class NumberedEnumLookup<T extends Enum<T> & NumberedEnum<T>> {

  private final Object[] values;

  public NumberedEnumLookup(T[] constants) {
    int max = 0;
    for (T constant : constants) {
      max = Math.max(max, constant.number());
    }
    Object[] table = new Object[max + 1];
    for (T constant : constants) {
      table[constant.number()] = constant;
    }
    this.values = table;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public T resolve(int number) {
    return number >= 0 && number < values.length ? (T) values[number] : null;
  }

  public T resolve(int number, T def) {
    T resolved = resolve(number);
    return resolved == null ? def : resolved;
  }

  public T require(int number) {
    T resolved = resolve(number);
    if (resolved == null) {
      throw new IllegalArgumentException("Unknown enum constant for number:" + number);
    }
    return resolved;
  }
}
