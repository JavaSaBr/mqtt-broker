package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class Qos2PublishOutHandler extends AbstractPublishOutHandler {

    @Override
    public @NotNull ActionResult handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber) {
        throw new UnsupportedOperationException();
    }
}
