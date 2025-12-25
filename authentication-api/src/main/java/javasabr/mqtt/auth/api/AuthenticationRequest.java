package javasabr.mqtt.auth.api;

public record AuthenticationRequest(
    String username,
    byte[] password,
    String authenticationMethod,
    byte[] authenticationData) {}
