package javasabr.mqtt.auth.api;

import java.util.Arrays;
import java.util.function.Function;
import javasabr.rlib.collections.dictionary.DictionaryCollectors;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AuthenticationMethod {
  X509("x509", 1),
  JWT("jwt", 3),
  OAUTH2("oauth2", 4),
  BASIC("basic", 5),
  LDAP("ldap", 6);

  private static final RefToRefDictionary<String, AuthenticationMethod> CACHE = Arrays.stream(values())
      .collect(DictionaryCollectors.toRefToRefDictionary(AuthenticationMethod::value, Function.identity()));

  String value;
  int priority;

  public static AuthenticationMethod fromValue(String value) {
    return CACHE.get(value);
  }
}
