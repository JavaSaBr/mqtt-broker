package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.service.MqttConnectionService;
import javasabr.mqtt.service.message.handler.MqttInMessageHandler;
import javasabr.rlib.network.packet.ReadableNetworkPacket;
import lombok.CustomLog;

@CustomLog
public class DefaultMqttConnectionService implements MqttConnectionService {

  MqttInMessageHandler[] inMessageHandlers;

  public DefaultMqttConnectionService(Collection<? extends MqttInMessageHandler> knownInMessageHandlers) {
    int highestPacketType = knownInMessageHandlers
        .stream()
        .mapToInt(MqttInMessageHandler::packetType)
        .max()
        .orElse(0);

    var inMessageHandlers = new MqttInMessageHandler[highestPacketType + 1];

    for (MqttInMessageHandler knownInMessageHandler : knownInMessageHandlers) {
      int packetType = knownInMessageHandler.packetType();
      if (inMessageHandlers[packetType] != null) {
        throw new IllegalArgumentException("Found duplicate MqttInMessageHandler:[" + knownInMessageHandler + "]");
      }
      inMessageHandlers[packetType] = knownInMessageHandler;
    }

    this.inMessageHandlers = inMessageHandlers;
  }

  @Override
  public void processAcceptedConnection(MqttConnection connection) {
    log.info(connection.remoteAddress(), "Accept new connection:[%s]"::formatted);
    connection.onReceive(this::processReceivedMessage);
  }

  protected void processReceivedMessage(
      MqttConnection connection,
      ReadableNetworkPacket<MqttConnection> networkPacket) {

    if (!(networkPacket instanceof MqttReadablePacket mrp)) {
      log.warning(networkPacket, "Received not processable network packet:[%s]"::formatted);
      return;
    }

    try {
      inMessageHandlers[mrp.packetType()].processReceived(connection, mrp);
    } catch (IndexOutOfBoundsException ex) {
      log.warning(mrp, "Received not supported MQTT message:[%s]"::formatted);
    }
  }
}
