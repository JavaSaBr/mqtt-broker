package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Qos1PublishOutHandler extends PersistentPublishOutHandler {

    @Override
    protected QoS getQoS() {
        return QoS.AT_LEAST_ONCE;
    }

    @Override
    public boolean handleResponse(MqttClient client, HasPacketId response) {

        if (!(response instanceof PublishAckInPacket)) {
            throw new IllegalStateException("Unexpected response: " + response);
        }

        // just return 'true' to remove pending packet from session
        return true;
    }
}
