package javasabr.mqtt.model.acl;

import java.util.List;

public record Rule(String name, int priority, String effect, String event, RuleClients clients, List<String> topics) {}
