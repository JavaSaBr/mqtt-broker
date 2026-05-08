package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractSubscriberPublishSender<U extends NetworkMqttUser>
    implements SubscriberPublishSender {

  Class<U> expectedUserType;
  MessageOutFactoryService messageOutFactoryService;
  IncomingPublishStorage incomingPublishStorage;

  @Override
  public final void sendToSubscriber(IncomingPublish incomingPublish, MqttUser user) {
    if (!expectedUserType.isInstance(user)) {
      log.warning(user.clientId(), user.getClass(), "[%s] Not expected user of type:[%s]"::formatted);
      return;
    }
    U expectedUser = expectedUserType.cast(user);
    MqttSession session = expectedUser.session();
    if (session == null) {
      log.warning(user.clientId(), "[%s] Session is already closed"::formatted);
      return;
    }
    OutgoingPublish outgoingPublish = buildOutgoingPublish(expectedUser, session, incomingPublish);
    if (outgoingPublish != null) {
      sendToSubscriberImpl(expectedUser, session, outgoingPublish);
    }
  }

  @Nullable
  protected abstract OutgoingPublish buildOutgoingPublish(
      U user, 
      MqttSession session, 
      IncomingPublish incomingPublish);

  protected void sendToSubscriberImpl(
      U user, 
      MqttSession session, 
      OutgoingPublish outgoingPublish) {
    send(user, outgoingPublish);
  }

  protected void send(U user, OutgoingPublish outgoingPublish) {
    MqttOutMessage mqttOutMessage = messageOutFactoryService
        .resolveFactory(user)
        .newPublish(
            outgoingPublish.messageId(),
            outgoingPublish.qos(),
            outgoingPublish.retained(),
            outgoingPublish.duplicated(),
            outgoingPublish.topicName(),
            outgoingPublish.topicAlias(),
            outgoingPublish.data(),
            outgoingPublish.responseTopicName(),
            outgoingPublish.userProperties());
    send(user, outgoingPublish, mqttOutMessage);
  }

  protected void send(
      U user, 
      OutgoingPublish outgoingPublish, 
      MqttOutMessage mqttOutMessage) {
    user.sendInBackground(mqttOutMessage);
  }
}
