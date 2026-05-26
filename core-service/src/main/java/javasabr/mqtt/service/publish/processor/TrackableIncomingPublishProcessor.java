package javasabr.mqtt.service.publish.processor;

import java.time.Duration;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.RetainPublishService;
import lombok.CustomLog;

@CustomLog
public abstract class TrackableIncomingPublishProcessor<U extends NetworkMqttUser> extends
    AbstractIncomingPublishProcessor<U> {

  protected static final Duration META_EXPIRATION = Duration.ofMinutes(10);

  public TrackableIncomingPublishProcessor(
      Class<U> expectedClientType,
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService,
      IncomingPublishStorage incomingPublishStorage) {
    super(
        expectedClientType,
        subscriptionService, 
        publishDispatcher,
        messageOutFactoryService, 
        retainPublishService,
        incomingPublishStorage);
  }

  @Override
  protected boolean validateImpl(U user, NetworkMqttSession session, IncomingPublish publish) {
    int messagedId = publish.messageId();
    if (messagedId == MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      handleMissedMessageId(user, publish);
      return false;
    }
    return super.validateImpl(user, session, publish);
  }

  @Override
  protected void processImpl(U user, NetworkMqttSession session, IncomingPublish publish) {
    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.add(publish.messageId(), MqttMessageType.PUBLISH, null, META_EXPIRATION);
    log.debug(user.clientId(), publish.messageId(), publish.id(), 
        "[%s] Register tracking messageId:[%s] for publish:[%s]"::formatted);
    super.processImpl(user, session, publish);
  }

  protected void handleMissedMessageId(U user, IncomingPublish publish) {
    log.debug(user.clientId(), publish.id(), "[%s] Missed required messageId in publish:[%s]"::formatted);
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID);
    user.closeWithReason(response);
  }
}
