package javasabr.mqtt.model.acl;

import java.util.List;

public record User(String name, String password, List<String> groups) {}
