package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Connect request.
 */
@RequiredArgsConstructor
public class Connect311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.CONNECT.ordinal();

    private final @NotNull String username;
    private final @NotNull String willTopic;
    private final @NotNull String clientId;

    private final @NotNull byte[] password;
    private final @NotNull byte[] willPayload;

    private final @NotNull QoS willQos;

    private final int keepAlive;

    private final boolean willRetain;
    private final boolean cleanStart;

    protected @NotNull MqttVersion getMqttVersion() {
        return MqttVersion.MQTT_3_1_1;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {

        var mqttVersion = getMqttVersion();

        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718030
        writeString(buffer, mqttVersion.getName());
        writeByte(buffer, mqttVersion.getVersion());
        writeByte(buffer, buildConnectFlags());
        writeShort(buffer, keepAlive);
    }

    @Override
    protected void writePayload(@NotNull ByteBuffer buffer) {

        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718031
        writeString(buffer, clientId);

        // for MQTT 5
        if (StringUtils.isNotEmpty(willTopic)) {
            appendWillProperties(buffer);
        }

        if (StringUtils.isNotEmpty(willTopic)) {
            writeString(buffer, willTopic);
            writeBytes(buffer, willPayload);
        }

        if (StringUtils.isNotEmpty(username)) {
            writeString(buffer, username);
        }

        if (ArrayUtils.isNotEmpty(password)) {
            writeBytes(buffer, password);
        }
    }

    private int buildConnectFlags() {

        int connectFlags = 0;

        if (StringUtils.isNotEmpty(username)) {
            connectFlags |= 0b1000_0000;
        }

        if (ArrayUtils.isNotEmpty(password)) {
            connectFlags |= 0b0100_0000;
        }

        if (StringUtils.isNotEmpty(willTopic)) {
            connectFlags |= 0b0000_0100;
            connectFlags |= (willQos.ordinal() << 3);
            if (willRetain) {
                connectFlags |= 0b0010_0000;
            }
        }

        if (cleanStart) {
            connectFlags |= 0b0000_0010;
        }

        return connectFlags;
    }

    protected void appendWillProperties(@NotNull ByteBuffer buffer) { }
}
