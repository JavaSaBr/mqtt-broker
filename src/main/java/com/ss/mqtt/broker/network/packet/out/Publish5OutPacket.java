package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class Publish5OutPacket extends Publish311OutPacket {

    private final boolean stringPayload;
    private final String responseTopic;
    private final byte[] correlationData;
    private final Array<StringPair> userProperties;

    public Publish5OutPacket(
        @NotNull MqttClient client,
        int packetId,
        int qos,
        boolean retained,
        boolean duplicate,
        @NotNull String topicName,
        @NotNull byte[] payload,
        boolean stringPayload,
        @NotNull String responseTopic,
        @NotNull byte[] correlationData,
        @NotNull Array<StringPair> userProperties
    ) {
        super(client, packetId, qos, retained, duplicate, topicName, payload);
        this.stringPayload = stringPayload;
        this.responseTopic = responseTopic;
        this.correlationData = correlationData;
        this.userProperties = userProperties;
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc511988586
        writeProperty(buffer, PacketProperty.PAYLOAD_FORMAT_INDICATOR, stringPayload);
        writeProperty(
            buffer,
            PacketProperty.MESSAGE_EXPIRY_INTERVAL,
            0,
            MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.TOPIC_ALIAS,
            client.getTopicAliasMaximum(),
            MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT
        );
        writeProperty(buffer, PacketProperty.RESPONSE_TOPIC, responseTopic);
        writeProperty(buffer, PacketProperty.CORRELATION_DATA, correlationData);
        writeUserProperties(buffer, userProperties);
    }
}
