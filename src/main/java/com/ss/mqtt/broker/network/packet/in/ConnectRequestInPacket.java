package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.ConnectReturnCode;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Connection request.
 */
public class ConnectRequestInPacket extends MqttReadablePacket {

    public static final byte PACKET_TYPE = (byte) PacketType.CONNECT_REQUEST.ordinal();

    public ConnectRequestInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readImpl(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        super.readImpl(connection, buffer);

        var client = connection.getClient();
        var protocolName = readString(buffer);
        var protocolLevel = buffer.get();
        var mqttVersion = MqttVersion.of(protocolName, protocolLevel);

        if (mqttVersion == MqttVersion.UNKNOWN) {
            client.reject(ConnectReturnCode.REJECTED_UNACCEPTABLE_PROTOCOL_VERSION);
            return;
        }

        var flags = readUnsignedByte(buffer);
        var keepAlive = readMsbLsbInt(buffer);
        var hasUserName = (flags & 0x80) == 0x80;
        var hasPassword = (flags & 0x40) == 0x40;
        var willRetain = (flags & 0x20) == 0x20;
        var willQos = (flags & 0x18) >> 3;
        var willFlag = (flags & 0x04) == 0x04;
        var cleanStart = (flags & 0x02) == 0x02;

        // for mqtt 3.1.1+
        if (mqttVersion.ordinal() >= MqttVersion.MQTT_3_1_1.ordinal()) {

            var zeroReservedFlag = (flags & 0x01) == 0x0;

            if (!zeroReservedFlag) {
                //The Server MUST validate that the reserved flag in the CONNECT packet is set to 0 [MQTT-3.1.2-3]. If
                //the reserved flag is not 0 it is a Malformed Packet. Refer to section 4.13 for information about handling
                //errors.
                throw new IllegalStateException("non-zero reserved flag");
            }
        }

        var clientId = readString(buffer);

        // FIXME validate client id here

        var willTopic = willFlag ? readString(buffer, 0, Short.MAX_VALUE) : null;
        var willMessage = willFlag ? readBytes(buffer) : null;
        var username = hasUserName ? readString(buffer) : null;
        var password = hasPassword ? readString(buffer) : null;

        client.onConnected(
            mqttVersion,
            clientId,
            username,
            password,
            willTopic,
            willMessage,
            keepAlive,
            willRetain,
            willQos,
            cleanStart
        );
    }
}
