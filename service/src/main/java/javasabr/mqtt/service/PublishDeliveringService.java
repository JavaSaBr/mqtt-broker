package javasabr.mqtt.service;

import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public interface PublishDeliveringService {

  PublishHandlingResult startDelivering(PublishInPacket publish, SingleSubscriber subscriber);
}
