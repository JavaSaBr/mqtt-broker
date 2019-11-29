package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Connect request.
 */
public class Connect5OutPacket extends Connect311OutPacket {

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

    private static final Set<PacketProperty> WILL_PROPERTIES = EnumSet.of(
        /*
          Followed by the Four Byte Integer representing the Will Delay Interval in seconds. It is a Protocol Error to
          include the Will Delay Interval more than once. If the Will Delay Interval is absent, the default value is 0
          and there is no delay before the Will Message is published.

          The Server delays publishing the Client’s Will Message until the Will Delay Interval has passed or the
          Session ends, whichever happens first. If a new Network Connection to this Session is made before the
          Will Delay Interval has passed, the Server MUST NOT send the Will Message
         */
        PacketProperty.WILL_DELAY_INTERVAL,
        /*
          Followed by the value of the Payload Format Indicator, either of:
          • 0 (0x00) Byte Indicates that the Will Message is unspecified bytes, which is equivalent to not
            sending a Payload Format Indicator.
          • 1 (0x01) Byte Indicates that the Will Message is UTF-8 Encoded Character Data. The UTF-8 data
            in the Payload MUST be well-formed UTF-8 as defined by the Unicode specification
            [Unicode] and restated in RFC 3629 [RFC3629].

          It is a Protocol Error to include the Payload Format Indicator more than once. The Server MAY validate
          that the Will Message is of the format indicated, and if it is not se
         */
        PacketProperty.PAYLOAD_FORMAT_INDICATOR,
        /*
          Followed by the Four Byte Integer representing the Message Expiry Interval. It is a Protocol Error to
          include the Message Expiry Interval more than once.

          If present, the Four Byte value is the lifetime of the Will Message in seconds and is sent as the
          Publication Expiry Interval when the Server publishes the Will Message.

          If absent, no Message Expiry Interval is sent when the Server publishes the Will Message.
         */
        PacketProperty.MESSAGE_EXPIRY_INTERVAL,
        /*
          Followed by a UTF-8 Encoded String describing the content of the Will Message. It is a Protocol Error to
          include the Content Type more than once. The value of the Content Type is defined by the sending and
          receiving application.
         */
        PacketProperty.CONTENT_TYPE,
        /*
          Followed by a UTF-8 Encoded String which is used as the Topic Name for a response message. It is a
          Protocol Error to include the Response Topic more than once. The presence of a Response Topic
          identifies the Will Message as a Request.
         */
        PacketProperty.RESPONSE_TOPIC,
        /*
          Followed by Binary Data. The Correlation Data is used by the sender of the Request Message to identify
          which request the Response Message is for when it is received. It is a Protocol Error to include
          Correlation Data more than once. If the Correlation Data is not present, the Requester does not require
          any correlation data.

          The value of the Correlation Data only has meaning to the sender of the Request Message and receiver
          of the Response Message.
         */
        PacketProperty.CORRELATION_DATA,
        /*
          Followed by a UTF-8 String Pair. The User Property is allowed to appear multiple times to represent
          multiple name, value pairs. The same name is allowed to appear more than once.

          The Server MUST maintain the order of User Properties when publishing the Will Message
         */
        PacketProperty.USER_PROPERTY
    );

    // properties
    private final @NotNull Array<StringPair> userProperties;
    private final @NotNull String authenticationMethod;
    private final @NotNull byte[] authenticationData;

    private final long sessionExpiryInterval;
    private final int receiveMax;
    private final int maximumPacketSize;
    private final int topicAliasMaximum;
    private final boolean requestResponseInformation;
    private final boolean requestProblemInformation;

    public Connect5OutPacket(@NotNull String clientId, int keepAlive) {
        this(
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            clientId,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            QoS.AT_MOST_ONCE,
            keepAlive,
            false,
            false,
            Array.empty(),
            StringUtils.EMPTY,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED,
            MqttPropertyConstants.RECEIVE_MAXIMUM_UNDEFINED,
            MqttPropertyConstants.MAXIMUM_PACKET_SIZE_UNDEFINED,
            MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_UNDEFINED,
            false,
            false
        );
    }

    public Connect5OutPacket(
        @NotNull String username,
        @NotNull String willTopic,
        @NotNull String clientId,
        @NotNull byte[] password,
        @NotNull byte[] willPayload,
        @NotNull QoS willQos,
        int keepAlive,
        boolean willRetain,
        boolean cleanStart,
        @NotNull Array<StringPair> userProperties,
        @NotNull String authenticationMethod,
        @NotNull byte[] authenticationData,
        long sessionExpiryInterval,
        int receiveMax,
        int maximumPacketSize,
        int topicAliasMaximum,
        boolean requestResponseInformation,
        boolean requestProblemInformation
    ) {
        super(username, willTopic, clientId, password, willPayload, willQos, keepAlive, willRetain, cleanStart);
        this.userProperties = userProperties;
        this.authenticationMethod = authenticationMethod;
        this.authenticationData = authenticationData;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.receiveMax = receiveMax;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.requestResponseInformation = requestResponseInformation;
        this.requestProblemInformation = requestProblemInformation;
    }

    protected @NotNull MqttVersion getMqttVersion() {
        return MqttVersion.MQTT_5;
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void appendWillProperties(@NotNull ByteBuffer buffer) {

        var propertiesBuffer = getPropertiesBuffer();

        writeWillProperties(propertiesBuffer);

        if (propertiesBuffer.position() < 1) {
            buffer.put((byte) 0);
            return;
        }

        propertiesBuffer.flip();

        MqttDataUtils.writeMbi(propertiesBuffer.limit(), buffer)
            .put(propertiesBuffer);
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901046
        writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
        writeNotEmptyProperty(buffer, PacketProperty.AUTHENTICATION_METHOD, authenticationMethod);
        writeNotEmptyProperty(buffer, PacketProperty.AUTHENTICATION_DATA, authenticationData);
        writeProperty(
            buffer,
            PacketProperty.REQUEST_RESPONSE_INFORMATION,
            requestResponseInformation,
            false
        );
        writeProperty(
            buffer,
            PacketProperty.REQUEST_PROBLEM_INFORMATION,
            requestProblemInformation,
            false
        );
        writeProperty(
            buffer,
            PacketProperty.RECEIVE_MAXIMUM,
            receiveMax,
            MqttPropertyConstants.RECEIVE_MAXIMUM_UNDEFINED
        );
        writeProperty(
            buffer,
            PacketProperty.TOPIC_ALIAS_MAXIMUM,
            topicAliasMaximum,
            MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_UNDEFINED
        );
        writeProperty(
            buffer,
            PacketProperty.SESSION_EXPIRY_INTERVAL,
            sessionExpiryInterval,
            MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED
        );
        writeProperty(
            buffer,
            PacketProperty.MAXIMUM_PACKET_SIZE,
            maximumPacketSize,
            MqttPropertyConstants.MAXIMUM_PACKET_SIZE_UNDEFINED
        );
    }

    protected void writeWillProperties(@NotNull ByteBuffer buffer) {
    }
}
