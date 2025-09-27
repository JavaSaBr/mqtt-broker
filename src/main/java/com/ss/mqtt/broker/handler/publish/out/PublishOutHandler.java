package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;

/**
 * Interface to handle outgoing publish packets.
 */
public interface PublishOutHandler {

    ActionResult handle(PublishInPacket packet, SingleSubscriber subscriber);
}
