package javasabr.mqtt.legacy.network.client;

import javasabr.mqtt.legacy.handler.client.MqttClientReleaseHandler;
import javasabr.mqtt.legacy.network.MqttConnection;
import javasabr.mqtt.base.utils.DebugUtils;

public class ExternalMqttClient extends AbstractMqttClient {

  static {
    DebugUtils.registerIncludedFields("clientId");
  }

  public ExternalMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    super(connection, releaseHandler);
  }
}
