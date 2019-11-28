package com.ss.mqtt.broker.handler.publish.out;

import static com.ss.mqtt.broker.model.ActionResult.SUCCESS;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

public class Qos0PublishOutHandler extends AbstractPublishOutHandler {

    @Override
    public @NotNull ActionResult handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber) {

        var client = subscriber.getMqttClient();
        var packetOutFactory = client.getPacketOutFactory();

        client.send(packetOutFactory.newPublish(
            client,
            MqttPropertyConstants.PACKET_ID_FOR_QOS_0,
            QoS.AT_MOST_ONCE_DELIVERY,
            packet.isRetained(),
            false,
            packet.getTopicName().toString(),
            MqttPropertyConstants.TOPIC_ALIAS_NOT_SET,
            packet.getPayload(),
            packet.isPayloadFormatIndicator(),
            packet.getResponseTopic(),
            packet.getCorrelationData(),
            packet.getUserProperties()
        ));
        return SUCCESS;
    }
}
