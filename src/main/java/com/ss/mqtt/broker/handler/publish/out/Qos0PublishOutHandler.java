package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

public class Qos0PublishOutHandler extends AbstractPublishOutHandler {

    @Override
    protected @NotNull QoS getQoS() {
        return QoS.AT_MOST_ONCE_DELIVERY;
    }

    @Override
    protected void handleImpl(
        @NotNull PublishInPacket packet,
        @NotNull Subscriber subscriber,
        @NotNull MqttClient client,
        @NotNull MqttSession session
    ) {
        sendPublish(client, packet, 0, false);
    }
}
