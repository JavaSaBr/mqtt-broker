package javasabr.mqtt.auth.api;

import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;

public record MqttCredentials(
    String username,
    byte[] password,
    AuthenticationMethod authenticationMethod,
    byte[] authenticationData) {

  public boolean isAnonymous() {
    return StringUtils.isEmpty(username) && ArrayUtils.isEmpty(password);
  }

  public boolean isMethodDefined() {
    return authenticationMethod != null;
  }
}
