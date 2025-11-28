package javasabr.mqtt.network.impl;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;

public class InternalNetworkMqttUser extends AbstractNetworkMqttUser {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  public InternalNetworkMqttUser(MqttConnection connection, NetworkMqttUserReleaseHandler releaseHandler) {
    super(connection, releaseHandler);
  }
}
