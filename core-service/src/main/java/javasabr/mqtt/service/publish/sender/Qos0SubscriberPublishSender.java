package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public class Qos0SubscriberPublishSender extends AbstractSubscriberPublishSender<ExternalNetworkMqttUser> {

  public Qos0SubscriberPublishSender(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Nullable
  @Override
  protected Publish buildOutgoing(
      ExternalNetworkMqttUser user,
      MqttSession session,
      Publish incoming) {
    return new OutgoingPublish(incoming, incoming.retained(), IntArray.EMPTY);
  }
}
