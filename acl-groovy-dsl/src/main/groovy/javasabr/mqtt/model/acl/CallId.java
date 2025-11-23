package javasabr.mqtt.model.acl;

public record CallId(String username, String clientId, String ipAddress, Action action, String topic) {}
