package javasabr.mqtt.auth.service.config;

public interface DatabaseUrlConfig {
  String driver();

  String host();

  int port();

  String name();
}

