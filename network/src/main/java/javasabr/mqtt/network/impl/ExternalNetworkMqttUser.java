package javasabr.mqtt.network.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;

public class ExternalNetworkMqttUser extends AbstractNetworkMqttUser {
  
  public ExternalNetworkMqttUser(MqttConnection connection, NetworkMqttUserReleaseHandler releaseHandler) {
    super(connection, releaseHandler);
  }
}
