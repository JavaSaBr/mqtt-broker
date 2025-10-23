package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.publish.handler.MqttPublishInMessageHandler;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultPublishReceivingService implements PublishReceivingService {

  @Nullable
  MqttPublishInMessageHandler[] publishInHandlers;

  public DefaultPublishReceivingService(
      Collection<? extends MqttPublishInMessageHandler> knownPublishInHandlers) {

    int maxIndex = knownPublishInHandlers
        .stream()
        .map(MqttPublishInMessageHandler::qos)
        .mapToInt(QoS::index)
        .max()
        .orElse(0);

    var publishInHandlers = new MqttPublishInMessageHandler[maxIndex + 1];

    for (MqttPublishInMessageHandler knownPublishInHandler : knownPublishInHandlers) {
      QoS qos = knownPublishInHandler.qos();
      if (publishInHandlers[qos.index()] != null) {
        throw new IllegalArgumentException(
            "Found duplicate MqttPublishInMessageHandler:[" + knownPublishInHandler + "]");
      }
      publishInHandlers[qos.index()] = knownPublishInHandler;
    }

    this.publishInHandlers = publishInHandlers;
  }

  @Override
  public void processReceivedPublish(MqttClient client, PublishInPacket publish) {
    try {
      //noinspection DataFlowIssue
      publishInHandlers[publish.getQos().index()].handle(client, publish);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(publish, "Received not supported publish message:[%s]"::formatted);
    }
  }
}
