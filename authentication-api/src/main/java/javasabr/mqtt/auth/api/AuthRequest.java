package javasabr.mqtt.auth.api;

public record AuthRequest(String username, byte[] password, String authenticationMethod, byte[] authenticationData) {}
