package javasabr.mqtt.network.session;

import java.time.Duration;

public interface ConfigurableNetworkMqttSession extends NetworkMqttSession {
 
  void expiryInterval(Duration expiryInterval);
}
