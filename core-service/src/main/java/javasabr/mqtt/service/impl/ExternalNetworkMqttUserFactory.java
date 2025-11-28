package javasabr.mqtt.service.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalNetworkMqttUserFactory implements NetworkMqttUserFactory {

  NetworkMqttUserReleaseHandler releaseHandler;

  @Override
  public ConfigurableNetworkMqttUser createNetworkUser(MqttConnection connection) {
    return new ExternalNetworkMqttUser(connection, releaseHandler);
  }
}
