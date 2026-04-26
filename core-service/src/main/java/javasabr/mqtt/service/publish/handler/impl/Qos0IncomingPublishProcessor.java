package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDispatcher;
import javasabr.mqtt.service.RetainPublishService;
import javasabr.mqtt.service.SubscriptionService;

public class Qos0IncomingPublishProcessor extends AbstractIncomingPublishProcessor<ExternalNetworkMqttUser> {

  public Qos0IncomingPublishProcessor(
      SubscriptionService subscriptionService,
      PublishDispatcher publishDispatcher,
      MessageOutFactoryService messageOutFactoryService,
      RetainPublishService retainPublishService) {
    super(
        ExternalNetworkMqttUser.class,
        subscriptionService, publishDispatcher,
        messageOutFactoryService, retainPublishService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected boolean validateImpl(ExternalNetworkMqttUser user, NetworkMqttSession session, Publish publish) {
    int messageId = publish.messageId();
    if (messageId != MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      handleNotExpectedMessageId(user);
      return false;
    }
    return super.validateImpl(user, session, publish);
  }

  private void handleNotExpectedMessageId(ExternalNetworkMqttUser user) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NOT_EXPECTED_MESSAGE_ID);
    user.closeWithReason(response);
  }
}
