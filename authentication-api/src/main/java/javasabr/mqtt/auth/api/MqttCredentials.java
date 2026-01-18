package javasabr.mqtt.auth.api;

import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import org.jspecify.annotations.Nullable;

public record MqttCredentials(
    String clientId,
    String username,
    byte[] password,
    @Nullable AuthenticationMethod authenticationMethod,
    byte[] authenticationData) {

  public boolean isAnonymous() {
    return StringUtils.isEmpty(username) && ArrayUtils.isEmpty(password);
  }

  public boolean isMethodDefined() {
    return authenticationMethod != null;
  }
}
