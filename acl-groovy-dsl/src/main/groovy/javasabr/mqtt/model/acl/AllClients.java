package javasabr.mqtt.model.acl;

import javasabr.rlib.collections.array.Array;

public record AllClients(
    Array<ClientMatcher> clientMatchers) implements Clients {

  public static final AllClients MATCH_ALL = new AllClients(Array.empty(ClientMatcher.class));

  @Override
  public boolean match(CallId callId) {
    for (ClientMatcher username : clientMatchers) {
      if (!username.match(callId)) {
        return false;
      }
    }
    return true;
  }
}
