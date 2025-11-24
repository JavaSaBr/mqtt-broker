package javasabr.mqtt.service.impl;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttClientFactory;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalMqttClientFactory implements MqttClientFactory {

  MqttClientReleaseHandler releaseHandler;

  @Override
  public UnsafeMqttClient newClient(MqttConnection connection) {
    return new ExternalMqttClient(connection, releaseHandler);
  }
}
