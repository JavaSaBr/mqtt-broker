package javasabr.mqtt.auth.api;

public record MqttCredentials(
    String username,
    byte[] password,
    String authenticationMethod,
    byte[] authenticationData) {}
