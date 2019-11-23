package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.handler.publish.in.PublishInHandler;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class DefaultPublishingService implements PublishingService {

    private final PublishInHandler @NotNull [] publishInHandlers;

    @Override
    public void publish(@NotNull MqttClient client, @NotNull PublishInPacket publish) {
        publishInHandlers[publish.getQos().ordinal()].handle(client, publish);
    }
}
