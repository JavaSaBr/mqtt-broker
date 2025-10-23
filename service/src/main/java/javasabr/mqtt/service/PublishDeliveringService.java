package javasabr.mqtt.service;

import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.packet.in.PublishInPacket;

public interface PublishDeliveringService {

  ActionResult startDelivering(PublishInPacket publish, SingleSubscriber subscriber);
}
