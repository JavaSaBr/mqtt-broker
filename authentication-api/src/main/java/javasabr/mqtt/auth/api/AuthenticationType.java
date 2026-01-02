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
public enum AuthenticationType {
  X509("x509"),
  IP_CIDR("cidr"),
  JWT("jwt"),
  OAUTH2("oauth2"),
  BASIC("basic"),
  LDAP("ldap"),
  ANONYMOUS("anon");

  private static final RefToRefDictionary<String, AuthenticationType> CACHE = Arrays.stream(values())
      .collect(DictionaryCollectors.toRefToRefDictionary(AuthenticationType::value, Function.identity()));

  String value;

  public static AuthenticationType fromValue(String value){
    return CACHE.get(value);
  }
}
