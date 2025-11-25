package javasabr.mqtt.model.acl;

import java.util.List;

public interface Clients {

  boolean match(CallId callId);

  default boolean check(List<ClientComparator> comparators, String value) {
    for (ClientComparator cc : comparators) {
      if (cc.compare(value)) {
        return true;
      }
    }
    return false;
  }
}
