package javasabr.mqtt.auth.api;

public record MqttCredentials(
    String username,
    byte[] password,
    AuthenticationMethod authenticationMethod,
    byte[] authenticationData) {}
