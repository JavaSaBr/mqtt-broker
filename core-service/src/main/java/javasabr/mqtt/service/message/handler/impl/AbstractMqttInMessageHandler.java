package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
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
public abstract class AbstractMqttInMessageHandler<U extends NetworkMqttUser, M extends MqttInMessage>
    implements MqttInMessageHandler {

  Class<U> expectedUser;
  Class<M> expectedMessage;
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public Class<? extends NetworkMqttUser> expectedUserType() {
    return expectedUser;
  }

  protected boolean requireSession() {
    return true;
  }

  @Override
  public final void processValidMessage(MqttConnection connection, MqttInMessage mqttInMessage) {
    NetworkMqttUser user = connection.user();
    if (!expectedUser.isInstance(user)) {
      log.warning(user, "Received not expected user:[%s]"::formatted);
      return;
    } else if (!expectedMessage.isInstance(mqttInMessage)) {
      log.warning(mqttInMessage, "Received not expected message:[%s]"::formatted);
      return;
    }
    U castedUser = expectedUser.cast(user);
    M castedMessage = expectedMessage.cast(mqttInMessage);
    if (requireSession()) {
      NetworkMqttSession session = user.session();
      if (session == null) {
        log.warning(user.clientId(), "[%s] Session is already closed"::formatted);
        handleSessionIsAlreadyClosed(user);
        return;
      }
      processValidMessage(connection, castedUser, session, castedMessage);
    } else {
      processValidMessage(connection, castedUser, castedMessage);
    }
  }

  @Override
  public final void processInvalidMessage(MqttConnection connection, MqttInMessage mqttInMessage) {
    NetworkMqttUser user = connection.user();
    if (!expectedUser.isInstance(user)) {
      log.warning(user, "Received not expected user:[%s]"::formatted);
      return;
    } else if (!expectedMessage.isInstance(mqttInMessage)) {
      log.warning(mqttInMessage, "Received not expected message:[%s]"::formatted);
      return;
    }
    U castedUser = expectedUser.cast(user);
    M castedMessage = expectedMessage.cast(mqttInMessage);
    if (requireSession()) {
      NetworkMqttSession session = user.session();
      if (session == null) {
        log.warning(user.clientId(), "[%s] Session is already closed"::formatted);
        handleSessionIsAlreadyClosed(user);
        return;
      }
      processInvalidMessage(connection, castedUser, session, castedMessage);
    } else {
      processInvalidMessage(connection, castedUser, castedMessage);
    }
  }

  protected void processValidMessage(MqttConnection connection, U user, M message) {}

  protected void processValidMessage(MqttConnection connection, U user, NetworkMqttSession session, M message) {}

  protected boolean processInvalidMessage(MqttConnection connection, U user, M message) {
    Exception exception = message.exception();
    if (exception instanceof MalformedProtocolMqttException) {
      malformedProtocolError(connection, user, exception);
      return true;
    }
    return false;
  }

  protected boolean processInvalidMessage(MqttConnection connection, U user, NetworkMqttSession session, M message) {
    Exception exception = message.exception();
    if (exception instanceof MalformedProtocolMqttException) {
      malformedProtocolError(connection, user, exception);
      return true;
    }
    return false;
  }

  protected void malformedProtocolError(MqttConnection connection, U user, Exception exception) {
    // send feedback and close connection
    MqttOutMessage feedback = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.MALFORMED_PACKET, exception.getMessage());
    user.sendAsync(feedback)
        .thenAccept(_ -> connection.close());
  }

  protected void handleSessionIsAlreadyClosed(NetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(
            user,
            DisconnectReasonCode.UNSPECIFIED_ERROR,
            ExtraErrorReasons.SESSION_IS_ALREADY_CLOSED);
    user.closeWithReason(response);
  }
}
