package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

public class Publish5OutPacket extends Publish311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the value of the Payload Forma t Indicator, either of:
            · 0 (0x00) Byte Indicates that the Payload is unspecified bytes, which is equivalent to not sending a
              Payload Format Indicator.
            · 1 (0x01) Byte Indicates that the Payload is UTF-8 Encoded Character Data. The UTF-8 data in the Payload
              MUST be well-formed UTF-8 as defined by the Unicode specification [Unicode] and restated in RFC 3629
              [RFC3629].

          A Server MUST send the Payload Format Indicator unaltered to all subscribers receiving the Application Message
          [MQTT-3.3.2-4]. The receiver MAY validate that the Payload is of the format indicated, and if it is not send a
          PUBACK, PUBREC, or DISCONNECT with Reason Code of 0x99 (Payload format invalid) as described in section 4.13.
          Refer to section 5.4.9 for information about security issues in validating the payload format.
         */
        PacketProperty.PAYLOAD_FORMAT_INDICATOR,
        /*
          Followed by the Four Byte Integer representing the Message Expiry Interval.
          If present, the Four Byte value is the lifetime of the Application Message in seconds. If the Message Expiry
          Interval has passed and the Server has not managed to start onward delivery to a matching subscriber, then it
          MUST delete the copy of the message for that subscriber [MQTT-3.3.2-5].

          If absent, the Application Message does not expire.

          The PUBLISH packet sent to a Client by the Server MUST contain a Message Expiry Interval set to the received
          value minus the time that the Application Message has been waiting in the Server [MQTT-3.3.2-6]. Refer to
          section 4.1 for details and limitations of stored state.
         */
        PacketProperty.MESSAGE_EXPIRY_INTERVAL,
        /*
          Followed by the Two Byte integer representing the Topic Alias value. It is a Protocol Error to include the
          Topic Alias value more than once.

          A Topic Alias is an integer value that is used to identify the Topic instead of using the Topic Name. This
          reduces the size of the PUBLISH packet, and is useful when the Topic Names are long and the same Topic Names
          are used repetitively within a Network Connection.

          The sender decides whether to use a Topic Alias and chooses the value. It sets a Topic Alias mapping by
          including a non-zero length Topic Name and a Topic Alias in the PUBLISH packet. The receiver processes the
          PUBLISH as normal but also sets the specified Topic Alias mapping to this Topic Name.

          If a Topic Alias mapping has been set at the receiver, a sender can send a PUBLISH packet that contains that
          Topic Alias and a zero length Topic Name. The receiver then treats the incoming PUBLISH as if it had contained
          the Topic Name of the Topic Alias.

          A sender can modify the Topic Alias mapping by sending another PUBLISH in the same Network Connection with the
          same Topic Alias value and a different non-zero length Topic Name.

          Topic Alias mappings exist only within a Network Connection and last only for the lifetime of that Network
          Connection. A receiver MUST NOT carry forward any Topic Alias mappings from one Network Connection to another
          [MQTT-3.3.2-7].

          A Topic Alias of 0 is not permitted. A sender MUST NOT send a PUBLISH packet containing a Topic Alias which
          has the value 0 [MQTT-3.3.2-8].

          A Client MUST NOT send a PUBLISH packet with a Topic Alias greater than the Topic Alias Maximum value returned
          by the Server in the CONNACK packet [MQTT-3.3.2-9]. A Client MUST accept all Topic Alias values greater than 0
          and less than or equal to the Topic Alias Maximum value that it sent in the CONNECT packet [MQTT-3.3.2-10].

          A Server MUST NOT send a PUBLISH packet with a Topic Alias greater than the Topic Alias Maximum value sent by
          the Client in the CONNECT packet [MQTT-3.3.2-11]. A Server MUST accept all Topic Alias values greater than 0
          and less than or equal to the Topic Alias Maximum value that it returned in the CONNACK packet [MQTT-3.3.2-12]

          The Topic Alias mappings used by the Client and Server are independent from each other. Thus, when a Client
          sends a PUBLISH containing a Topic Alias value of 1 to a Server and the Server sends a PUBLISH with a Topic
          Alias value of 1 to that Client they will in general be referring to different Topics.
         */
        PacketProperty.TOPIC_ALIAS,
        /*
          Followed by a UTF-8 Encoded String which is used as the Topic Name for a response message. The Response Topic
          MUST be a UTF-8 Encoded String as defined in section 1.5.4 [MQTT-3.3.2-13]. The Response Topic MUST NOT
          contain wildcard characters [MQTT-3.3.2-14]. It is a Protocol Error to include the Response Topic more than
          once. The presence of a Response Topic identifies the Message as a Request.

          Refer to section 4.10 for more information about Request / Response.

          The Server MUST send the Response Topic unaltered to all subscribers receiving the Application Message
          [MQTT-3.3.2-15].

          Non-normative comment:
          The receiver of an Application Message with a Response Topic sends a response by using the Response Topic as
          the Topic Name of a PUBLISH. If the Request Message contains a Correlation Data, the receiver of the Request
          Message should also include this Correlation Data as a property in the PUBLISH packet of the Response Message.
         */
        PacketProperty.RESPONSE_TOPIC,
        /*
          Followed by Binary Data. The Correlation Data is used by the sender of the Request Message to identify which
          request the Response Message is for when it is received. It is a Protocol Error to include Correlation Data
          more than once. If the Correlation Data is not present, the Requester does not require any correlation data.

          The Server MUST send the Correlation Data unaltered to all subscribers receiving the Application Message
          [MQTT-3.3.2-16]. The value of the Correlation Data only has meaning to the sender of the Request Message and
          receiver of the Response Message.

          Non-normative comment
          The receiver of an Application Message which contains both a Response Topic and a Correlation Data sends a
          response by using the Response Topic as the Topic Name of a PUBLISH. The Client should also send the
          Correlation Data unaltered as part of the PUBLISH of the responses.

          Non-normative comment
          If the Correlation Data contains information which can cause application failures if modified by the Client
          responding to the request, it should be encrypted and/or hashed to allow any alteration to be detected.

          Refer to section 4.10 for more information about Request / Response
         */
        PacketProperty.CORRELATION_DATA,
        /*
          Followed by a UTF-8 String Pair. The User Property is allowed to appear multiple times to represent multiple
          name, value pairs. The same name is allowed to appear more than once.

          The Server MUST send all User Properties unaltered in a PUBLISH packet when forwarding the Application Message
          to a Client [MQTT-3.3.2-17]. The Server MUST maintain the order of User Properties when forwarding the
          Application Message [MQTT-3.3.2-18].

          Non-normative comment
          This property is intended to provide a means of transferring application layer name-value tags whose meaning
          and interpretation are known only by the application programs responsible for sending and receiving them.
         */
        PacketProperty.USER_PROPERTY
    );


    private final int topciAlias;
    private final boolean stringPayload;
    private final @NotNull String responseTopic;
    private final @NotNull byte[] correlationData;
    private final @NotNull Array<StringPair> userProperties;

    public Publish5OutPacket(
        @NotNull MqttClient client,
        int packetId,
        @NotNull QoS qos,
        boolean retained,
        boolean duplicate,
        @NotNull String topicName,
        int topciAlias,
        @NotNull byte[] payload,
        boolean stringPayload,
        @NotNull String responseTopic,
        @NotNull byte[] correlationData,
        @NotNull Array<StringPair> userProperties
    ) {
        super(client, packetId, qos, retained, duplicate, topicName, payload);
        this.topciAlias = topciAlias;
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
        if (topciAlias > MqttPropertyConstants.TOPIC_ALIAS_MIN && topciAlias < MqttPropertyConstants.TOPIC_ALIAS_MAX) {
            writeProperty(
                buffer,
                PacketProperty.TOPIC_ALIAS,
                topciAlias,
                MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT
            );
        }
        if (responseTopic != null) {
            writeProperty(buffer, PacketProperty.RESPONSE_TOPIC, responseTopic);
        }
        if (correlationData != null) {
            writeProperty(buffer, PacketProperty.CORRELATION_DATA, correlationData);
        }
        writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
    }

}
