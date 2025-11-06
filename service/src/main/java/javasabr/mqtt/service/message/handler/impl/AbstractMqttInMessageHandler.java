package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.message.handler.MqttInMessageHandler;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractMqttInMessageHandler<C extends MqttClient, M extends MqttInMessage>
    implements MqttInMessageHandler {

  Class<C> expectedClient;
  Class<M> expectedNetworkPacket;
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public void processReceived(MqttConnection connection, MqttInMessage message) {
    MqttClient client = connection.client();
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Received not expected client:[%s]"::formatted);
      return;
    } else if (!expectedNetworkPacket.isInstance(message)) {
      log.warning(message, "Received not expected network packet:[%s]"::formatted);
      return;
    }

    C castedClient = expectedClient.cast(client);
    M castedMessage = expectedNetworkPacket.cast(message);
    if (checkMessageException(connection, castedClient, castedMessage)) {
      return;
    }
    processReceived(connection, castedClient, castedMessage);
  }

  protected abstract void processReceived(MqttConnection connection, C client, M message);

  protected boolean checkMessageException(MqttConnection connection, C client, M message) {
    Exception exception = message.exception();
    if (exception instanceof MalformedProtocolMqttException) {
      // send feedback and close connection
      MqttOutMessage feedback = messageOutFactoryService
          .resolveFactory(client)
          .newDisconnect(client, DisconnectReasonCode.MALFORMED_PACKET);
      client
          .sendWithFeedback(feedback)
          .thenAccept(_ -> connection.close());
      return true;
    }
    return false;
  }
}
