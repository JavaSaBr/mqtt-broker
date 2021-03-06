package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to handle outgoing publish packets.
 */
public interface PublishOutHandler {

    @NotNull ActionResult handle(@NotNull PublishInPacket packet, @NotNull SingleSubscriber subscriber);
}
