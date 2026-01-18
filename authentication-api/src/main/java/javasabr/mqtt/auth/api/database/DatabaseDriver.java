package javasabr.mqtt.auth.api.database;

import java.util.Collection;
import java.util.Set;
import javasabr.rlib.common.AliasedEnum;
import javasabr.rlib.common.util.AliasedEnumMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DatabaseDriver implements AliasedEnum<DatabaseDriver> {
  POSTGRESQL("postgresql");

  public static final AliasedEnumMap<DatabaseDriver> BY_ALIAS = new AliasedEnumMap<>(DatabaseDriver.class);

  String value;

  @Override
  public Collection<String> aliases() {
    return Set.of(value);
  }
}
