package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.message.handler.MqttInMessageHandler;
import javasabr.rlib.network.packet.ReadableNetworkPacket;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultConnectionService implements ConnectionService {

  @Nullable
  MqttInMessageHandler[] inMessageHandlers;

  public DefaultConnectionService(Collection<? extends MqttInMessageHandler> knownInMessageHandlers) {
    int highestPacketType = knownInMessageHandlers
        .stream()
        .map(MqttInMessageHandler::messageType)
        .mapToInt(MqttMessageType::typeIndex)
        .max()
        .orElse(0);

    var inMessageHandlers = new MqttInMessageHandler[highestPacketType + 1];

    for (MqttInMessageHandler knownInMessageHandler : knownInMessageHandlers) {
      MqttMessageType messageType = knownInMessageHandler.messageType();
      if (inMessageHandlers[messageType.typeIndex()] != null) {
        throw new IllegalArgumentException("Found duplicate MqttInMessageHandler:[" + knownInMessageHandler + "]");
      }
      inMessageHandlers[messageType.typeIndex()] = knownInMessageHandler;
    }

    this.inMessageHandlers = inMessageHandlers;
    log.info(inMessageHandlers, DefaultConnectionService::buildServiceDescription);
  }

  @Override
  public void processAcceptedConnection(MqttConnection connection) {
    log.info(connection.remoteAddress(), "Accept new connection:[%s]"::formatted);
    connection.onReceive(this::processReceivedMessage);
  }

  protected void processReceivedMessage(
      MqttConnection connection,
      ReadableNetworkPacket<MqttConnection> networkPacket) {

    if (!(networkPacket instanceof MqttInMessage mrp)) {
      log.warning(networkPacket, "Received not processable network packet:[%s]"::formatted);
      return;
    }

    log.debug(
        connection.client().clientId(),
        networkPacket.name(),
        networkPacket,
        "[%s] Received from client message:[%s] %s"::formatted);

    try {
      //noinspection DataFlowIssue
      inMessageHandlers[mrp.messageType()].processReceived(connection, mrp);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(mrp, "Received not supported MQTT message:[%s]"::formatted);
    }
  }

  private static String buildServiceDescription(@Nullable MqttInMessageHandler[] inMessageHandlers) {
    var builder = new StringBuilder();
    builder.append("{\n");
    int count = 0;
    for (MqttInMessageHandler inMessageHandler : inMessageHandlers) {
      if (inMessageHandler == null) {
        continue;
      }
      count++;
      builder
          .append("  \"")
          .append(inMessageHandler.messageType())
          .append("\": \"")
          .append(inMessageHandler
              .getClass()
              .getSimpleName())
          .append("\",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Registered [%s] MqttInMessageHandlers: %s".formatted(count, builder);
  }
}
