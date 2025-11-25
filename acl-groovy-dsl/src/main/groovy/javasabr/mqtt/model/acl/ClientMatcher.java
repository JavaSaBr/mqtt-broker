package javasabr.mqtt.model.acl;

import java.util.function.Function;
import javasabr.rlib.collections.array.Array;

public record ClientMatcher(
    Function<CallId, String> valueGetter,
    Array<ClientComparator> ruleMatchers) {

  public boolean match(CallId callId) {
    for (ClientComparator username : ruleMatchers) {
      if (username.compare(valueGetter.apply(callId))) {
        return true;
      }
    }
    return false;
  }
}
