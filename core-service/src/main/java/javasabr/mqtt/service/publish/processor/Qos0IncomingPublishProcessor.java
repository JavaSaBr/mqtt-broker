package javasabr.mqtt.service.publish.processor;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.RetainPublishService;

public class Qos0IncomingPublishProcessor extends AbstractIncomingPublishProcessor<ExternalNetworkMqttUser> {

  public Qos0IncomingPublishProcessor(
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService,
      IncomingPublishStorage incomingPublishStorage) {
    super(
        ExternalNetworkMqttUser.class,
        subscriptionService,
        publishDispatcher,
        messageOutFactoryService,
        retainPublishService,
        incomingPublishStorage);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected boolean validateImpl(
      ExternalNetworkMqttUser user, 
      NetworkMqttSession session, 
      IncomingPublish publish) {
    int messageId = publish.messageId();
    if (messageId != MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      handleNotExpectedMessageId(user, publish);
      return false;
    }
    return super.validateImpl(user, session, publish);
  }

  @Override
  protected void processImpl(ExternalNetworkMqttUser user, NetworkMqttSession session, IncomingPublish publish) {
    super.processImpl(user, session, publish);
    dispatchToSubscriber(user, session, publish);
  }

  private void handleNotExpectedMessageId(ExternalNetworkMqttUser user, IncomingPublish publish) {
    incomingPublishStorage.removeIfExist(publish);
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NOT_EXPECTED_MESSAGE_ID);
    user.closeWithReason(response);
  }
}
