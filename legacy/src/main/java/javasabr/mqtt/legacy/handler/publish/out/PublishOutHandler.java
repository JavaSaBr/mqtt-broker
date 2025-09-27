package javasabr.mqtt.legacy.handler.publish.out;

import javasabr.mqtt.legacy.model.ActionResult;
import javasabr.mqtt.legacy.model.SingleSubscriber;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;

/**
 * Interface to handle outgoing publish packets.
 */
public interface PublishOutHandler {

  ActionResult handle(PublishInPacket packet, SingleSubscriber subscriber);
}
