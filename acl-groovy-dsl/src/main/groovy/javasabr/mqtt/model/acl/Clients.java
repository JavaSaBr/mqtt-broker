package javasabr.mqtt.model.acl;

import java.util.List;

public record Clients(Operator operator, List<String> usernames, List<String> clientIds, List<String> ipAddresses) {
  public static final Clients ALL = new Clients(null, null, null, null);
}
