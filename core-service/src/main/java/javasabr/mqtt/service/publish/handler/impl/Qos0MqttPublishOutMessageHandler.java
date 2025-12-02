package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import org.jspecify.annotations.Nullable;

public class Qos0MqttPublishOutMessageHandler 
    extends AbstractMqttPublishOutMessageHandler<ExternalNetworkMqttUser> {

  public Qos0MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, subscriptionService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Nullable
  @Override
  protected Publish reconstruct(
      ExternalNetworkMqttUser user,
      MqttSession session,
      Publish original) {
    return original.with(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_NOT_SET);
  }
}
