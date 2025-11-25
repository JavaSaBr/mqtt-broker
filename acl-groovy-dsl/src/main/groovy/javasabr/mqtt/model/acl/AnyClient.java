package javasabr.mqtt.model.acl;

import javasabr.rlib.collections.array.Array;

public record AnyClient(
    Array<ClientMatcher> clientMatchers) implements Clients {

  @Override
  public boolean match(CallId callId) {
    for (ClientMatcher username : clientMatchers) {
      if (username.match(callId)) {
        return true;
      }
    }
    return false;
  }
}
