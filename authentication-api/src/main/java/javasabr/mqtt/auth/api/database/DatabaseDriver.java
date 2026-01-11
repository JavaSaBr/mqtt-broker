package javasabr.mqtt.auth.api.database;

import java.util.Arrays;
import java.util.function.Function;
import javasabr.rlib.collections.dictionary.DictionaryCollectors;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DatabaseDriver {
  POSTGRESQL("postgresql");

  private static final RefToRefDictionary<String, DatabaseDriver> CACHE = Arrays.stream(values())
      .collect(DictionaryCollectors.toRefToRefDictionary(DatabaseDriver::value, Function.identity()));

  String value;

  public static DatabaseDriver fromValue(String value) {
    return StringUtils.isEmpty(value) ? null : CACHE.get(value);
  }
}
