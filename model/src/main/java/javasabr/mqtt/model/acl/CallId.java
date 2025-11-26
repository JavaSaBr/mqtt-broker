package javasabr.mqtt.model.acl;

public record CallId(String username, String clientId, String ipAddress, Operation operation, String topic) {}
