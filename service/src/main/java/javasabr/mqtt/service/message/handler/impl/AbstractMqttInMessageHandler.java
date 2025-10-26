package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.service.message.handler.MqttInMessageHandler;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractMqttInMessageHandler<C extends MqttClient, P extends MqttReadablePacket>
    implements MqttInMessageHandler {

  Class<C> expectedClient;
  Class<P> expectedNetworkPacket;

  @Override
  public void processReceived(MqttConnection connection, MqttReadablePacket networkPacket) {
    MqttClient client = connection.client();
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Received not expected client:[%s]"::formatted);
      return;
    } else if (!expectedNetworkPacket.isInstance(networkPacket)) {
      log.warning(networkPacket, "Received not expected network packet:[%s]"::formatted);
      return;
    }
    processReceived(
        connection,
        expectedClient.cast(client),
        expectedNetworkPacket.cast(networkPacket));
  }

  protected abstract void processReceived(MqttConnection connection, C client, P networkPacket);
}
