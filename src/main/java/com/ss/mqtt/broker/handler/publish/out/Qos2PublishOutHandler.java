package com.ss.mqtt.broker.handler.publish.out;

import static com.ss.mqtt.broker.model.reason.code.PublishReleaseReasonCode.SUCCESS;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishCompleteInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishReceivedInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class Qos2PublishOutHandler extends PersistentPublishOutHandler {

    @Override
    protected @NotNull QoS getQoS() {
        return QoS.EXACTLY_ONCE_DELIVERY;
    }

    @Override
    public boolean handleResponse(@NotNull MqttClient client, @NotNull HasPacketId response) {

        var packetOutFactory = client.getPacketOutFactory();

        if (response instanceof PublishReceivedInPacket) {
            client.send(packetOutFactory.newPublishRelease(client, response.getPacketId(), SUCCESS));
            return false;
        } else if (response instanceof PublishCompleteInPacket) {
            return true;
        } else {
            throw new IllegalStateException("Unexpected response: " + response);
        }
    }
}
