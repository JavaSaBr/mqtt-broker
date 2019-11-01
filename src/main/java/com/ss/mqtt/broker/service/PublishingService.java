package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

/**
 * Publishing service
 */
public interface PublishingService {

    /**
     * Sends publish packet to all subscribers
     *
     * @param publish publish packet to send
     * @return publish ack reason code
     */
    @NotNull PublishAckReasonCode publish(@NotNull PublishInPacket publish);

}
