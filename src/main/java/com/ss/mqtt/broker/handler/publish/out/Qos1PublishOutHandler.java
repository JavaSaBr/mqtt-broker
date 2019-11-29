package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class Qos1PublishOutHandler extends PersistentPublishOutHandler {

    @Override
    protected @NotNull QoS getQoS() {
        return QoS.AT_LEAST_ONCE_DELIVERY;
    }

    @Override
    public boolean handleResponse(@NotNull MqttClient client, @NotNull HasPacketId response) {

        if (!(response instanceof PublishAckInPacket)) {
            throw new IllegalStateException("Unexpected response: " + response);
        }

        // just return 'true' to remove pending packet from session
        return true;
    }
}
