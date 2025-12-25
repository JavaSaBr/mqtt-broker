package javasabr.mqtt.auth.service.config;

import java.util.Map;

public interface DatabaseUsersConfig {

  Credentials ANONYMOUS_CREDENTIAL = new Credentials("", "");

  Map<String, Credentials> users();

  default Credentials get(String username) {
    return users().getOrDefault(username, ANONYMOUS_CREDENTIAL);
  }
}

