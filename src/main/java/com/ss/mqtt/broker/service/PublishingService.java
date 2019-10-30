package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public interface PublishingService {

    @NotNull PublishAckReasonCode publish(@NotNull MqttClient mqttClient, @NotNull PublishInPacket publish);

}
