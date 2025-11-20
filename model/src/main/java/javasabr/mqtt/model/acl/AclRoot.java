package javasabr.mqtt.model.acl;

import java.util.List;

public record AclRoot(AclConfig acl, List<User> user, List<Group> group, List<Rule> rule) {}

