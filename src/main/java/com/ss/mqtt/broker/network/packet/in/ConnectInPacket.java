package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.NumberUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Connection request.
 */
public class ConnectInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.CONNECT_REQUEST.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          If the Session Expiry Interval is absent the value 0 is used. If it is set to 0,
          or is absent, the Session ends when the Network Connection is closed.
          If the Session Expiry Interval is 0xFFFFFFFF (UINT_MAX), the Session does not expire.

          The Client and Server MUST store the Session State after the Network Connection is closed if the
          Session Expiry Interval is greater than 0
         */
        PacketProperty.SESSION_EXPIRY_INTERVAL,
        /*
          Followed by the Two Byte Integer representing the Receive Maximum value. It is a Protocol Error to
          include the Receive Maximum value more than once or for it to have the value 0.

          The Client uses this value to limit the number of QoS 1 and QoS 2 publications that it is willing to process
          concurrently. There is no mechanism to limit the QoS 0 publications that the Server might try to send.

          The value of Receive Maximum applies only to the current Network Connection. If the Receive Maximum
          value is absent then its value defaults to 65,535.
         */
        PacketProperty.RECEIVE_MAXIMUM,
        /*
          Followed by a Four Byte Integer representing the Maximum Packet Size the Client is willing to accept. If
          the Maximum Packet Size is not present, no limit on the packet size is imposed beyond the limitations in
          the protocol as a result of the remaining length encoding and the protocol header sizes.

          It is a Protocol Error to include the Maximum Packet Size more than once, or for the value to be set to
          zero.
         */
        PacketProperty.MAXIMUM_PACKET_SIZE,
        /*
          Followed by the Two Byte Integer representing the Topic Alias Maximum value. It is a Protocol Error to
          include the Topic Alias Maximum value more than once. If the Topic Alias Maximum property is absent,
          the default value is 0.

          This value indicates the highest value that the Client will accept as a Topic Alias sent by the Server. The
          Client uses this value to limit the number of Topic Aliases that it is willing to hold on this Connection. The
          Server MUST NOT send a Topic Alias in a PUBLISH packet to the Client greater than Topic Alias
          Maximum [MQTT-3.1.2-26]. A value of 0 indicates that the Client does not accept any Topic Aliases on
          this connection. If Topic Alias Maximum is absent or zero, the Server MUST NOT send any Topic Aliases
          to the Client [MQTT-3.1.2-27].
         */
        PacketProperty.TOPIC_ALIAS_MAXIMUM,
        /*
          Followed by a Byte with a value of either 0 or 1. It is Protocol Error to include the Request Response
          Information more than once, or to have a value other than 0 or 1. If the Request Response Information is
          absent, the value of 0 is used.

          The Client uses this value to request the Server to return Response Information in the CONNACK. A
          value of 0 indicates that the Server MUST NOT return Response Information [MQTT-3.1.2-28]. If the
          value is 1 the Server MAY return Response Information in the CONNACK packet.
         */
        PacketProperty.REQUEST_RESPONSE_INFORMATION,
        /*
          Followed by a Byte with a value of either 0 or 1. It is a Protocol Error to include Request Problem
          Information more than once, or to have a value other than 0 or 1. If the Request Problem Information is
          8absent, the value of 1 is used.

          The Client uses this value to indicate whether the Reason String or User Properties are sent in the case
          of failures.

          If the value of Request Problem Information is 0, the Server MAY return a Reason String or User
          Properties on a CONNACK or DISCONNECT packet, but MUST NOT send a Reason String or User
          Properties on any packet other than PUBLISH, CONNACK, or DISCONNECT [MQTT-3.1.2-29]. If the
          value is 0 and the Client receives a Reason String or User Properties in a packet other than PUBLISH,
          CONNACK, or DISCONNECT, it uses a DISCONNECT packet with Reason Code 0x82 (Protocol Error)
          as described in section 4.13 Handling errors.

          If this value is 1, the Server MAY return a Reason String or User Properties on any packet where it is
          allowed.
         */
        PacketProperty.REQUEST_PROBLEM_INFORMATION,
        /*
          The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
          name is allowed to appear more than once
         */
        PacketProperty.USER_PROPERTY,
        /*
          Followed by a UTF-8 Encoded String containing the name of the authentication method used for
          extended authentication .It is a Protocol Error to include Authentication Method more than once.
          If Authentication Method is absent, extended authentication is not performed. Refer to section 4.12.

          If a Client sets an Authentication Method in the CONNECT, the Client MUST NOT send any packets other
          than AUTH or DISCONNECT packets until it has received a CONNACK packet
         */
        PacketProperty.AUTHENTICATION_METHOD,
        /*
          Followed by Binary Data containing authentication data. It is a Protocol Error to include Authentication
          Data if there is no Authentication Method. It is a Protocol Error to include Authentication Data more than
          once.

          The contents of this data are defined by the authentication method. Refer to section 4.12 for more
          information about extended authentication.
         */
        PacketProperty.AUTHENTICATION_DATA
    );

    private @Getter MqttVersion mqttVersion;

    private @Getter String clientId;
    private @Getter String willTopic;
    private @Getter String username;
    private @Getter String password;

    private @Getter byte[] willMessage;

    private @Getter int keepAlive;
    private @Getter int willQos;

    private @Getter boolean willRetain;
    private @Getter boolean cleanStart;

    public ConnectInPacket(byte info) {
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

        mqttVersion = MqttVersion.of(protocolName, protocolLevel);

        if (mqttVersion == MqttVersion.UNKNOWN) {
            client.reject(ConnectReasonCode.UNSUPPORTED_PROTOCOL_VERSION);
            return;
        }

        var flags = readUnsignedByte(buffer);

        keepAlive = readMsbLsbInt(buffer);
        willRetain = NumberUtils.isSetBit(flags, 5);
        willQos = (flags & 0x18) >> 3;
        cleanStart = NumberUtils.isSetBit(flags, 1);

        // for mqtt 3.1.1+
        if (mqttVersion.ordinal() >= MqttVersion.MQTT_3_1_1.ordinal()) {

            var zeroReservedFlag = NumberUtils.isNotSetBit(flags, 0);

            if (!zeroReservedFlag) {
                //The Server MUST validate that the reserved flag in the CONNECT packet is set to 0 [MQTT-3.1.2-3]. If
                //the reserved flag is not 0 it is a Malformed Packet. Refer to section 4.13 for information about handling
                //errors.
                throw new IllegalStateException("non-zero reserved flag");
            }
        }

        var hasUserName = NumberUtils.isSetBit(flags, 7);
        var hasPassword = NumberUtils.isSetBit(flags, 6);
        var willFlag = NumberUtils.isSetBit(flags, 2);

        if (mqttVersion.ordinal() >= MqttVersion.MQTT_5.ordinal()) {
            readProperties(buffer);
        }

        clientId = readString(buffer);

        // FIXME validate client id here

        willTopic = willFlag ? readString(buffer, 0, Short.MAX_VALUE) : null;
        willMessage = willFlag ? readBytes(buffer) : null;
        username = hasUserName ? readString(buffer) : null;
        password = hasPassword ? readString(buffer) : null;

        client.onConnected(this);
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull byte[] value) {
        super.applyProperty(property, value);
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
        super.applyProperty(property, value);
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, int value) {
        super.applyProperty(property, value);
    }
}
