package javasabr.mqtt.model.acl;

import java.util.List;

public record Rule(String name, Permission permission, Action action, RuleClients clients, List<String> topics) {}
