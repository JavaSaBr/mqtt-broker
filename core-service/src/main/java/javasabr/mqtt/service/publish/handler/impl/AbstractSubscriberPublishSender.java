package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.publish.handler.SubscriberPublishSender;
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

  @Override
  public final void sendToSubscriber(Publish publish, MqttUser user) {
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
    Publish publishToSend = reconstruct(expectedUser, session, publish);
    if (publishToSend != null) {
      handleImpl(expectedUser, session, publishToSend);
    }
  }

  @Nullable
  protected abstract Publish reconstruct(U user, MqttSession session, Publish original);

  protected void handleImpl(U user, MqttSession session, Publish publish) {
    send(user, publish);
  }

  protected void send(U user, Publish publish) {
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newPublish(
            publish.messageId(),
            publish.qos(),
            publish.retained(),
            publish.duplicated(),
            publish.topicName(),
            publish.topicAlias(),
            publish.payload(),
            publish.payloadFormat(),
            publish.responseTopicName(),
            publish.correlationData(),
            publish.userProperties()));
  }
}
