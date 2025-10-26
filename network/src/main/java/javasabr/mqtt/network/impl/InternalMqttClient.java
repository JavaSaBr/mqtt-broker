package javasabr.mqtt.network.impl;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;

public class InternalMqttClient extends AbstractMqttClient {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  public InternalMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    super(connection, releaseHandler);
  }
}
