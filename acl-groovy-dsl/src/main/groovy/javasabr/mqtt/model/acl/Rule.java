package javasabr.mqtt.model.acl;

import java.util.List;

public record Rule(
    Permission permission,
    Action action,
    Clients clients,
    List<String> topics) {
  public Rule(Permission permission,Action action){
    this(permission, action, AllClients.MATCH_ALL, List.of());
  }
}
