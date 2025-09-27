package javasabr.mqtt.legacy.network.client;

import javasabr.mqtt.legacy.handler.client.MqttClientReleaseHandler;
import javasabr.mqtt.legacy.network.MqttConnection;
import javasabr.mqtt.legacy.util.DebugUtils;

public class InternalMqttClient extends AbstractMqttClient {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  public InternalMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    super(connection, releaseHandler);
  }
}
