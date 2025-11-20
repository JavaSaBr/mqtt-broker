package javasabr.mqtt.model.acl;

import java.util.List;

public record RuleClients(List<String> users, List<String> ipAddresses) {}
