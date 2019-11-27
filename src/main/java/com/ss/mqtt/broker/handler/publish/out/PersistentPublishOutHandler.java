package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

public abstract class PersistentPublishOutHandler extends AbstractPublishOutHandler implements
    MqttSession.PendingPacketHandler {

    @Override
    protected void handleImpl(
        @NotNull PublishInPacket packet,
        @NotNull Subscriber subscriber,
        @NotNull MqttClient client,
        @NotNull MqttSession session
    ) {
        // generate new uniq packet id per client
        var packetId = session.nextPacketId();

        // register waiting async response
        session.registerOutPublish(packet, this, packetId);

        // send publish
        sendPublish(client, packet, packetId, false);
    }

    @Override
    public void resend(@NotNull MqttClient client, @NotNull PublishInPacket packet, int packetId) {
        sendPublish(client, packet, packetId, true);
    }
}
