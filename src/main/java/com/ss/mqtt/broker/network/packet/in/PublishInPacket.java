package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.IntegerArray;
import com.ss.rlib.common.util.array.MutableIntegerArray;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Publish message.
 */
@Getter
public class PublishInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the value of the Payload Format Indicator, either of:
          • 0 (0x00) Byte Indicates that the Payload is unspecified bytes, which is equivalent to not sending a
          Payload Format Indicator.
          • 1 (0x01) Byte Indicates that the Payload is UTF-8 Encoded Character Data. The UTF-8 data in
          the Payload MUST be well-formed UTF-8 as defined by the Unicode specification [Unicode]
          and restated in RFC 3629 [RFC3629].

          A Server MUST send the Payload Format Indicator unaltered to all subscribers receiving the Application
          1563 Message [MQTT-3.3.2-4]. The receiver MAY validate that the Payload is of the format indicated, and if it
          is not send a PUBACK, PUBREC, or DISCONNECT with Reason Code of 0x99 (Payload format invalid)
          as described in section 4.13. Refer to section 5.4.9 for information about security issues in validating the
          payload format.
         */
        PacketProperty.PAYLOAD_FORMAT_INDICATOR,
        /*
          Followed by the Four Byte Integer representing the Message Expiry Interval.

          If present, the Four Byte value is the lifetime of the Application Message in seconds. If the Message
          Expiry Interval has passed and the Server has not managed to start onward delivery to a matching
          subscriber, then it MUST delete the copy of the message for that subscriber [MQTT-3.3.2-5].

          If absent, the Application Message does not expire.

          The PUBLISH packet sent to a Client by the Server MUST contain a Message Expiry Interval set to the
          received value minus the time that the Application Message has been waiting in the Server [MQTT-3.3.2-
          6]. Refer to section 4.1 for details and limitations of stored state.
         */
        PacketProperty.MESSAGE_EXPIRY_INTERVAL,
        /*
          Followed by the Two Byte integer representing the Topic Alias value. It is a Protocol Error to include the
          Topic Alias value more than once.

          A Topic Alias is an integer value that is used to identify the Topic instead of using the Topic Name. This
          reduces the size of the PUBLISH packet, and is useful when the Topic Names are long and the same
          Topic Names are used repetitively within a Network Connection.

          The sender decides whether to use a Topic Alias and chooses the value. It sets a Topic Alias mapping by
          including a non-zero length Topic Name and a Topic Alias in the PUBLISH packet. The receiver
          processes the PUBLISH as normal but also sets the specified Topic Alias mapping to this Topic Name.

          If a Topic Alias mapping has been set at the receiver, a sender can send a PUBLISH packet that contains
          that Topic Alias and a zero length Topic Name. The receiver then treats the incoming PUBLISH as if it
          had contained the Topic Name of the Topic Alias.

          A sender can modify the Topic Alias mapping by sending another PUBLISH in the same Network
          Connection with the same Topic Alias value and a different non-zero length Topic Name.

          Topic Alias mappings exist only within a Network Connection and last only for the lifetime of that Network
          Connection. A receiver MUST NOT carry forward any Topic Alias mappings from one Network
          Connection to another [MQTT-3.3.2-7].

          A Topic Alias of 0 is not permitted. A sender MUST NOT send a PUBLISH packet containing a Topic
          Alias which has the value 0 [MQTT-3.3.2-8].

          A Client MUST NOT send a PUBLISH packet with a Topic Alias greater than the Topic Alias Maximum
          value returned by the Server in the CONNACK packet [MQTT-3.3.2-9]. A Client MUST accept all Topic
          Alias values greater than 0 and less than or equal to the Topic Alias Maximum value that it sent in the
          CONNECT packet [MQTT-3.3.2-10].

          A Server MUST NOT send a PUBLISH packet with a Topic Alias greater than the Topic Alias Maximum
          value sent by the Client in the CONNECT packet [MQTT-3.3.2-11]. A Server MUST accept all Topic Alias
          values greater than 0 and less than or equal to the Topic Alias Maximum value that it returned in the
          CONNACK packet [MQTT-3.3.2-12].

          The Topic Alias mappings used by the Client and Server are independent from each other. Thus, when a
          Client sends a PUBLISH containing a Topic Alias value of 1 to a Server and the Server sends a PUBLISH
          with a Topic Alias value of 1 to that Client they will in general be referring to different Topics.
         */
        PacketProperty.TOPIC_ALIAS,
        /*
          Followed by a UTF-8 Encoded String which is used as the Topic Name for a response message. The
          Response Topic MUST be a UTF-8 Encoded String as defined in section 1.5.4 [MQTT-3.3.2-13]. The
          Response Topic MUST NOT contain wildcard characters [MQTT-3.3.2-14]. It is a Protocol Error to include
          the Response Topic more than once. The presence of a Response Topic identifies the Message as a
          Request.

          Refer to section 4.10 for more information about Request / Response.

          The Server MUST send the Response Topic unaltered to all subscribers receiving the Application
          Message [MQTT-3.3.2-15].
         */
        PacketProperty.RESPONSE_TOPIC,
        /*
          Followed by Binary Data. The Correlation Data is used by the sender of the Request Message to identify
          which request the Response Message is for when it is received. It is a Protocol Error to include
          Correlation Data more than once. If the Correlation Data is not present, the Requester does not require
          any correlation data.

          The Server MUST send the Correlation Data unaltered to all subscribers receiving the Application
          Message [MQTT-3.3.2-16]. The value of the Correlation Data only has meaning to the sender of the
          Request Message and receiver of the Response Message.
         */
        PacketProperty.CORRELATION_DATA,
        /*
          Followed by a UTF-8 String Pair. The User Property is allowed to appear multiple times to represent
          multiple name, value pairs. The same name is allowed to appear more than once.

          The Server MUST send all User Properties unaltered in a PUBLISH packet when forwarding the
          Application Message to a Client [MQTT-3.3.2-17]. The Server MUST maintain the order of User
          Properties when forwarding the Application Message [MQTT-3.3.2-18].
         */
        PacketProperty.USER_PROPERTY,
        /*
          Followed by a Variable Byte Integer representing the identifier of the subscription.

          The Subscription Identifier can have the value of 1 to 268,435,455. It is a Protocol Error if the
          Subscription Identifier has a value of 0. Multiple Subscription Identifiers will be included if the publication
          is the result of a match to more than one subscription, in this case their order is not significant.
         */
        PacketProperty.SUBSCRIPTION_IDENTIFIER,
        /*
          Followed by a UTF-8 Encoded String describing the content of the Application Message. The Content
          Type MUST be a UTF-8 Encoded String as defined in section 1.5.4 [MQTT-3.3.2-19].
          It is a Protocol Error to include the Content Type more than once. The value of the Content Type is
          defined by the sending and receiving application.

          A Server MUST send the Content Type unaltered to all subscribers receiving the Application Message
          [MQTT-3.3.2-20].
         */
        PacketProperty.CONTENT_TYPE
    );

    /**
     * This field indicates the level of assurance for delivery of an Application Message. The QoS levels are
     * shown below.
     * <p>
     * 0 - 00 At most once delivery
     * 1 --01 At least once delivery
     * 2 - 10 Exactly once delivery
     * - - 11 Reserved – must not be used
     * <p>
     * If the Server included a Maximum QoS in its CONNACK response to a Client and it receives a PUBLISH
     * packet with a QoS greater than this, then it uses DISCONNECT with Reason Code 0x9B (QoS not
     * supported) as described in section 4.13 Handling errors.
     * <p>
     * A PUBLISH Packet MUST NOT have both QoS bits set to 1 [MQTT-3.3.1-4]. If a Server or Client receives
     * a PUBLISH packet which has both QoS bits set to 1 it is a Malformed Packet. Use DISCONNECT with
     * Reason Code 0x81 (Malformed Packet) as described in
     */
    private final QoS qos;

    /**
     * If the DUP flag is set to 0, it indicates that this is the first occasion that the Client or Server has attempted
     * to send this PUBLISH packet. If the DUP flag is set to 1, it indicates that this might be re-delivery of an
     * earlier attempt to send the packet.
     * <p>
     * The DUP flag MUST be set to 1 by the Client or Server when it attempts to re-deliver a PUBLISH packet
     * [MQTT-3.3.1-1]. The DUP flag MUST be set to 0 for all QoS 0 messages [MQTT-3.3.1-2].
     * <p>
     * The value of the DUP flag from an incoming PUBLISH packet is not propagated when the PUBLISH
     * packet is sent to subscribers by the Server. The DUP flag in the outgoing PUBLISH packet is set
     * independently to the incoming PUBLISH packet, its value MUST be determined solely by whether the
     * outgoing PUBLISH packet is a retransmission
     */
    private final boolean duplicate;

    /**
     * If the RETAIN flag is set to 1 in a PUBLISH packet sent by a Client to a Server, the Server MUST replace
     * any existing retained message for this topic and store the Application Message [MQTT-3.3.1-5], so that it
     * can be delivered to future subscribers whose subscriptions match its Topic Name. If the Payload contains
     * zero bytes it is processed normally by the Server but any retained message with the same topic name
     * MUST be removed and any future subscribers for the topic will not receive a retained message [MQTT1476 3.3.1-6].
     * A retained message with a Payload containing zero bytes MUST NOT be stored as a retained
     * message on the Server [MQTT-3.3.1-7].
     * <p>
     * If the RETAIN flag is 0 in a PUBLISH packet sent by a Client to a Server, the Server MUST NOT store the
     * message as a retained message and MUST NOT remove or replace any existing retained message
     * [MQTT-3.3.1-8].
     * <p>
     * If the Server included Retain Available in its CONNACK response to a Client with its value set to 0 and it
     * receives a PUBLISH packet with the RETAIN flag is set to 1, then it uses the DISCONNECT Reason
     * Code of 0x9A (Retain not supported) as described in section 4.13.
     * <p>
     * When a new Non-shared Subscription is made, the last retained message, if any, on each matching topic
     * name is sent to the Client as directed by the Retain Handling Subscription Option. These messages are
     * sent with the RETAIN flag set to 1. Which retained messages are sent is controlled by the Retain
     * Handling Subscription Option. At the time of the Subscription:
     * • If Retain Handling is set to 0 the Server MUST send the retained messages matching the Topic
     * Filter of the subscription to the Client [MQTT-3.3.1-9].
     * • If Retain Handling is set to 1 then if the subscription did not already exist, the Server MUST send
     * all retained message matching the Topic Filter of the subscription to the Client, and if the
     * subscription did exist the Server MUST NOT send the retained messages. [MQTT-3.3.1-10].
     * • If Retain Handling is set to 2, the Server MUST NOT send the retained messages
     * <p>
     * If the Server receives a PUBLISH packet with the RETAIN flag set to 1, and QoS 0 it SHOULD store the
     * new QoS 0 message as the new retained message for that topic, but MAY choose to discard it at any
     * time. If this happens there will be no retained message for that topic.
     * <p>
     * If the current retained message for a Topic expires, it is discarded and there will be no retained message
     * for that topic.
     * <p>
     * The setting of the RETAIN flag in an Application Message forwarded by the Server from an established
     * connection is controlled by the Retain As Published subscription option. Refer to section 3.8.3.1 for a
     * definition of the Subscription Options.
     * • If the value of Retain As Published subscription option is set to 0, the Server MUST set the RETAIN
     * flag to 0 when forwarding an Application Message regardless of how the RETAIN flag was set in the
     * received PUBLISH packet [MQTT-3.3.1-12].
     * • If the value of Retain As Published subscription option is set to 1, the Server MUST set the RETAIN
     * flag equal to the RETAIN flag in the received PUBLISH packet
     */
    private final boolean retained;

    /**
     * The list of subscription ids.
     */
    private @NotNull IntegerArray subscriptionIds;

    /**
     * The Topic Name identifies the information channel to which Payload data is published.
     * <p>
     * The Topic Name MUST be present as the first field in the PUBLISH packet Variable Header. It MUST be
     * a UTF-8 Encoded String as defined in section 1.5.4 [MQTT-3.3.2-1].
     * <p>
     * Standards Track Work Product Copyright © OASIS Open 2019. All Rights Reserved. Page 56 of 137
     * The Topic Name in the PUBLISH packet MUST NOT contain wildcard characters [MQTT-3.3.2-2].
     * <p>
     * The Topic Name in a PUBLISH packet sent by a Server to a subscribing Client MUST match the
     * Subscription’s Topic Filter according to the matching process defined in section 4.7 [MQTT-3.3.2-3].
     * However, as the Server is permitted to map the Topic Name to another name, it might not be the same as
     * the Topic Name in the original PUBLISH packet.
     * <p>
     * To reduce the size of the PUBLISH packet the sender can use a Topic Alias. The Topic Alias is described
     * in section 3.3.2.3.4. It is a Protocol Error if the Topic Name is zero length and there is no Topic Alias.
     */
    private @NotNull String topicName;

    /**
     * The response topic.
     */
    private @NotNull String responseTopic;

    /**
     * The content type.
     */
    private @NotNull String contentType;

    /**
     * The correlation data.
     */
    private @NotNull byte[] correlationData;

    /**
     * The payload data.
     */
    private @NotNull byte[] payload;

    /**
     * The Packet Identifier field is only present in PUBLISH packets where the QoS level is 1 or 2. Section
     * 2.2.1 provides more information about Packet Identifiers.
     */
    private int packetId;

    private long messageExpiryInterval = MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_DEFAULT;
    private int topicAlias = MqttPropertyConstants.TOPIC_ALIAS_DEFAULT;

    private boolean payloadFormatIndicator = MqttPropertyConstants.PAYLOAD_FORMAT_INDICATOR_DEFAULT;

    public PublishInPacket(byte info) {
        super(info);
        this.qos = QoS.of((info >> 1) & 0x03);
        this.retained = NumberUtils.isSetBit(info, 0);
        this.duplicate = NumberUtils.isSetBit(info, 3);
        this.topicName = StringUtils.EMPTY;
        this.responseTopic = StringUtils.EMPTY;
        this.contentType = StringUtils.EMPTY;
        this.correlationData = ArrayUtils.EMPTY_BYTE_ARRAY;
        this.payload = ArrayUtils.EMPTY_BYTE_ARRAY;
        this.subscriptionIds = IntegerArray.EMPTY;
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718039
        topicName = readString(buffer);
        packetId = qos != QoS.AT_MOST_ONCE_DELIVERY ? readUnsignedShort(buffer) : 0;
    }

    @Override
    protected void readPayload(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718040
        payload = readPayload(buffer);
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, long value) {
        switch (property) {
            case PAYLOAD_FORMAT_INDICATOR:
                payloadFormatIndicator = NumberUtils.toBoolean(value);
                break;
            case TOPIC_ALIAS:
                topicAlias = NumberUtils.validate(
                    (int) value,
                    MqttPropertyConstants.TOPIC_ALIAS_MIN,
                    MqttPropertyConstants.TOPIC_ALIAS_MAX)
                ;
                break;
            case MESSAGE_EXPIRY_INTERVAL:
                messageExpiryInterval = value;
                break;
            case SUBSCRIPTION_IDENTIFIER:
                if (subscriptionIds == IntegerArray.EMPTY) {
                    subscriptionIds = ArrayFactory.newMutableIntegerArray();
                }
                if (subscriptionIds instanceof MutableIntegerArray) {
                    ((MutableIntegerArray) subscriptionIds).add((int) value);
                }
                break;
            default:
                unexpectedProperty(property);
        }
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
        switch (property) {
            case RESPONSE_TOPIC:
                // TODO should be validated
                responseTopic = value;
                break;
            case CONTENT_TYPE:
                contentType = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull byte[] value) {
        switch (property) {
            case CORRELATION_DATA:
                correlationData = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }
}
