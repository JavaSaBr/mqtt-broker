package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.publish.handler.MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultPublishDeliveringService implements PublishDeliveringService {

  @Nullable
  MqttPublishOutMessageHandler[] publishOutMessageHandlers;

  public DefaultPublishDeliveringService(
      Collection<? extends MqttPublishOutMessageHandler> knownPublishOutHandlers) {

    int maxIndex = knownPublishOutHandlers
        .stream()
        .map(MqttPublishOutMessageHandler::qos)
        .mapToInt(QoS::index)
        .max()
        .orElse(0);

    var publishOutHandlers = new MqttPublishOutMessageHandler[maxIndex + 1];

    for (MqttPublishOutMessageHandler knownPublishOutHandler : knownPublishOutHandlers) {
      QoS qos = knownPublishOutHandler.qos();
      if (publishOutHandlers[qos.index()] != null) {
        throw new IllegalArgumentException(
            "Found duplicate MqttPublishOutMessageHandler:[" + knownPublishOutHandler + "]");
      }
      publishOutHandlers[qos.index()] = knownPublishOutHandler;
    }

    this.publishOutMessageHandlers = publishOutHandlers;
  }

  @Override
  public PublishHandlingResult startDelivering(PublishInPacket publish, SingleSubscriber subscriber) {
    try {
      //noinspection DataFlowIssue
      return publishOutMessageHandlers[publish.getQos().index()].handle(publish, subscriber);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(publish, "Received not supported publish message:[%s]"::formatted);
      return PublishHandlingResult.UNSPECIFIED_ERROR;
    }
  }
}
