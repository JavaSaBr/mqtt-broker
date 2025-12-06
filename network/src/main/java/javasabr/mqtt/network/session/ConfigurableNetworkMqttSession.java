package javasabr.mqtt.network.session;

public interface ConfigurableNetworkMqttSession extends NetworkMqttSession {
 
  void expirationTime(long expirationTime);
}
