package javasabr.mqtt.auth.credentials.source.config;

public interface DatabaseTimeoutsConfig {

  String lockTimeout();

  String statementTimeout();
}

