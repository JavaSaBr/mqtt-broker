package javasabr.mqtt.network.impl;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;

public class ExternalNetworkMqttUser extends AbstractNetworkMqttUser {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  public ExternalNetworkMqttUser(MqttConnection connection, NetworkMqttUserReleaseHandler releaseHandler) {
    super(connection, releaseHandler);
  }
}
