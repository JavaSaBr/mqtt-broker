package javasabr.mqtt.auth.api;

public record MqttCredentials(
    String username,
    byte[] password,
    AuthenticationType authenticationMethod,
    byte[] authenticationData) {}
