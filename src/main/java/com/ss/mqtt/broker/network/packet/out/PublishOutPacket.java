package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.PacketType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class PublishOutPacket extends MqttWritablePacket implements HasPacketId {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH.ordinal();

    protected final @Getter int packetId;

    public PublishOutPacket(@NotNull MqttClient client, int packetId) {
        super(client);
        this.packetId = packetId;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
