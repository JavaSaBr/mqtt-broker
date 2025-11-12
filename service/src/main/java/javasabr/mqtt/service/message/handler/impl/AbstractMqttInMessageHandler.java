package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.network.util.ExtraErrorReasons;
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
  public Class<? extends MqttClient> expectedClientType() {
    return expectedClient;
  }

  protected boolean requireSession() {
    return true;
  }

  @Override
  public void processValidMessage(MqttConnection connection, MqttInMessage mqttInMessage) {
    MqttClient client = connection.client();
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Received not expected client:[%s]"::formatted);
      return;
    } else if (!expectedNetworkPacket.isInstance(mqttInMessage)) {
      log.warning(mqttInMessage, "Received not expected network packet:[%s]"::formatted);
      return;
    }
    C castedClient = expectedClient.cast(client);
    M castedMessage = expectedNetworkPacket.cast(mqttInMessage);
    if (requireSession()) {
      MqttSession session = client.session();
      if (session == null) {
        log.warning(client.clientId(), "[%s] Session is already closed"::formatted);
        handleSessionIsAlreadyClosed(client);
        return;
      }
      processValidMessage(connection, castedClient, session, castedMessage);
    } else {
      processValidMessage(connection, castedClient, castedMessage);
    }
  }

  @Override
  public void processInvalidMessage(MqttConnection connection, MqttInMessage mqttInMessage) {
    MqttClient client = connection.client();
    if (!expectedClient.isInstance(client)) {
      log.warning(client, "Received not expected client:[%s]"::formatted);
      return;
    } else if (!expectedNetworkPacket.isInstance(mqttInMessage)) {
      log.warning(mqttInMessage, "Received not expected network packet:[%s]"::formatted);
      return;
    }
    C castedClient = expectedClient.cast(client);
    M castedMessage = expectedNetworkPacket.cast(mqttInMessage);
    if (requireSession()) {
      MqttSession session = client.session();
      if (session == null) {
        log.warning(client.clientId(), "[%s] Session is already closed"::formatted);
        handleSessionIsAlreadyClosed(client);
        return;
      }
      processInvalidMessage(connection, castedClient, session, castedMessage);
    } else {
      processInvalidMessage(connection, castedClient, castedMessage);
    }
  }

  protected void processValidMessage(MqttConnection connection, C client, M message) {}

  protected void processValidMessage(MqttConnection connection, C client, MqttSession session, M message) {}

  protected boolean processInvalidMessage(MqttConnection connection, C client, M message) {
    Exception exception = message.exception();
    if (exception instanceof MalformedProtocolMqttException) {
      malformedProtocolError(connection, client, exception);
      return true;
    }
    return false;
  }

  protected boolean processInvalidMessage(MqttConnection connection, C client, MqttSession session, M message) {
    Exception exception = message.exception();
    if (exception instanceof MalformedProtocolMqttException) {
      malformedProtocolError(connection, client, exception);
      return true;
    }
    return false;
  }

  protected void malformedProtocolError(MqttConnection connection, C client, Exception exception) {
    // send feedback and close connection
    MqttOutMessage feedback = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.MALFORMED_PACKET, exception.getMessage());
    client
        .sendWithFeedback(feedback)
        .thenAccept(_ -> connection.close());
  }

  protected void handleSessionIsAlreadyClosed(MqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(
            client,
            DisconnectReasonCode.UNSPECIFIED_ERROR,
            ExtraErrorReasons.SESSION_IS_ALREADY_CLOSED);
    client.closeWithReason(response);
  }
}
