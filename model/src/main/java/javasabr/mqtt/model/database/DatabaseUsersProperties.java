package javasabr.mqtt.model.database;

import java.util.Map;
import javasabr.mqtt.model.Credentials;

public interface DatabaseUsersProperties {

  Credentials ANONYMOUS_CREDENTIAL = new Credentials("", "");

  Map<String, Credentials> users();

  default Credentials get(String username) {
    return users().getOrDefault(username, ANONYMOUS_CREDENTIAL);
  }
}

