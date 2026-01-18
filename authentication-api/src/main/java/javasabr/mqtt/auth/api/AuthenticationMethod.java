package javasabr.mqtt.auth.api;

import java.util.Collection;
import java.util.List;
import javasabr.rlib.common.AliasedEnum;
import javasabr.rlib.common.util.AliasedEnumMap;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AuthenticationMethod implements AliasedEnum<AuthenticationMethod> {
  X509("x509", 1),
  JWT("jwt", 3),
  OAUTH2("oauth2", 4),
  BASIC("basic", 5),
  LDAP("ldap", 6);

  public static final AliasedEnumMap<AuthenticationMethod> MAP = new AliasedEnumMap<>(AuthenticationMethod.class);

  String value;
  int priority;

  @Nullable
  public static AuthenticationMethod fromValue(@Nullable String value) {
    return StringUtils.isEmpty(value) ? null : MAP.resolve(value);
  }

  @Override
  public Collection<String> aliases() {
    return List.of(value);
  }
}
