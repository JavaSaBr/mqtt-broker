package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
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
  public final void sendToSubscriber(IncomingPublish incoming, MqttUser user) {
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
    OutgoingPublish outgoing = buildOutgoing(expectedUser, session, incoming);
    if (outgoing != null) {
      sendToSubscriberImpl(expectedUser, session, outgoing);
    }
  }

  @Nullable
  protected abstract OutgoingPublish buildOutgoing(U user, MqttSession session, IncomingPublish incoming);

  protected void sendToSubscriberImpl(U user, MqttSession session, OutgoingPublish outgoing) {
    send(user, outgoing);
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
            publish.data(),
            publish.responseTopicName(),
            publish.userProperties()));
  }
}
