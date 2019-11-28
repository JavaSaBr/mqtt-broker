package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to handle outgoing publish packets.
 */
public interface PublishOutHandler {

    boolean handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber);
}
