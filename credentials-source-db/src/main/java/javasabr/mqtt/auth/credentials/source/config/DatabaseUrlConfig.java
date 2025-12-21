package javasabr.mqtt.auth.credentials.source.config;

public interface DatabaseUrlConfig {
  String driver();

  String host();

  int port();

  String name();
}

