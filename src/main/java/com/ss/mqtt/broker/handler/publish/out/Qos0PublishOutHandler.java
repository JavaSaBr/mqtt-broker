package com.ss.mqtt.broker.handler.publish.out;

import static com.ss.mqtt.broker.model.ActionResult.SUCCESS;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

public class Qos0PublishOutHandler extends AbstractPublishOutHandler {

    @Override
    protected @NotNull QoS getQoS() {
        return QoS.AT_MOST_ONCE_DELIVERY;
    }

    @Override
    protected @NotNull ActionResult handleImpl(
        @NotNull PublishInPacket packet,
        @NotNull Subscriber subscriber,
        @NotNull MqttClient client,
        @NotNull MqttSession session
    ) {
        sendPublish(client, packet, 0, false);
        return SUCCESS;
    }
}
