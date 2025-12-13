package javasabr.mqtt.model.auth;

public record AuthRequest(String username, byte[] password, String authenticationMethod, byte[] authenticationData) {}
