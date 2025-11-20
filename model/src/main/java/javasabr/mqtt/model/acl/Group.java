package javasabr.mqtt.model.acl;

import java.util.List;

public record Group(String name, List<String> users) {}
