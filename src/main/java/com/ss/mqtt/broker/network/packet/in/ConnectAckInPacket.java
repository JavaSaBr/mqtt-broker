package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Acknowledge connection request.
 */
@Getter
public class ConnectAckInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.CONNECT_ACK.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the Four Byte Integer representing the Session Expiry Interval in seconds. It is a Protocol
          Error to include the Session Expiry Interval more than once.

          If the Session Expiry Interval is absent the value in the CONNECT Packet used. The server uses this
          property to inform the Client that it is using a value other than that sent by the Client in the CONNACK.
         */
        PacketProperty.SESSION_EXPIRY_INTERVAL,
        /*
          Followed by the Two Byte Integer representing the Receive Maximum value. It is a Protocol Error to
          include the Receive Maximum value more than once or for it to have the value 0.

          The Server uses this value to limit the number of QoS 1 and QoS 2 publications that it is willing to
          process concurrently for the Client. It does not provide a mechanism to limit the QoS 0 publications that
          the Client might try to send.

          If the Receive Maximum value is absent, then its value defaults to 65,535.
         */
        PacketProperty.RECEIVE_MAXIMUM,
        /*
          Followed by a Byte with a value of either 0 or 1. It is a Protocol Error to include Maximum QoS more than
          once, or to have a value other than 0 or 1. If the Maximum QoS is absent, the Client uses a Maximum
          QoS of 2.

          If a Server does not support QoS 1 or QoS 2 PUBLISH packets it MUST send a Maximum QoS in the
          CONNACK packet specifying the highest QoS it supports [MQTT-3.2.2-9]. A Server that does not support
          QoS 1 or QoS 2 PUBLISH packets MUST still accept SUBSCRIBE packets containing a Requested QoS
          of 0, 1 or 2 [MQTT-3.2.2-10].

          If a Client receives a Maximum QoS from a Server, it MUST NOT send PUBLISH packets at a QoS level
          exceeding the Maximum QoS level specified [MQTT-3.2.2-11]. It is a Protocol Error if the Server receives
          a PUBLISH packet with a QoS greater than the Maximum QoS it specified. In this case use
          DISCONNECT with Reason Code 0x9B (QoS not supported) as described in section 4.13 Handling
          errors.

          If a Server receives a CONNECT packet containing a Will QoS that exceeds its capabilities, it MUST
          reject the connection. It SHOULD use a CONNACK packet with Reason Code 0x9B (QoS not supported)
          as described in section 4.13 Handling errors, and MUST close the Network Connection
         */
        PacketProperty.MAXIMUM_QOS,
        /*
          Followed by a Byte field. If present, this byte declares whether the Server supports retained messages. A
          value of 0 means that retained messages are not supported. A value of 1 means retained messages are
          supported. If not present, then retained messages are supported. It is a Protocol Error to include Retain
          Available more than once or to use a value other than 0 or 1.

          If a Server receives a CONNECT packet containing a Will Message with the Will Retain set to 1, and it
          does not support retained messages, the Server MUST reject the connection request. It SHOULD send
          CONNACK with Reason Code 0x9A (Retain not supported) and then it MUST close the Network
          Connection [MQTT-3.2.2-13].

          A Client receiving Retain Available set to 0 from the Server MUST NOT send a PUBLISH packet with the
          RETAIN flag set to 1 [MQTT-3.2.2-14]. If the Server receives such a packet, this is a Protocol Error. The
          Server SHOULD send a DISCONNECT with Reason Code of 0x9A (Retain not supported) as described
          in section 4.13.
         */
        PacketProperty.RETAIN_AVAILABLE,
        /*
          Followed by a Four Byte Integer representing the Maximum Packet Size the Server is willing to accept. If
          the Maximum Packet Size is not present, there is no limit on the packet size imposed beyond the
          limitations in the protocol as a result of the remaining length encoding and the protocol header sizes.

          It is a Protocol Error to include the Maximum Packet Size more than once, or for the value to be set to
          zero.

          The packet size is the total number of bytes in an MQTT Control Packet, as defined in section 2.1.4. The
          Server uses the Maximum Packet Size to inform the Client that it will not process packets whose size
          exceeds this limit.

          The Client MUST NOT send packets exceeding Maximum Packet Size to the Server [MQTT-3.2.2-15]. If
          a Server receives a packet whose size exceeds this limit, this is a Protocol Error, the Server uses
          DISCONNECT with Reason Code 0x95 (Packet too large), as described in section 4.13.
         */
        PacketProperty.MAXIMUM_PACKET_SIZE,
        /*
          Followed by the UTF-8 string which is the Assigned Client Identifier. It is a Protocol Error to include the
          Assigned Client Identifier more than once.

          The Client Identifier which was assigned by the Server because a zero length Client Identifier was found
          in the CONNECT packet.

          If the Client connects using a zero length Client Identifier, the Server MUST respond with a CONNACK
          containing an Assigned Client Identifier. The Assigned Client Identifier MUST be a new Client Identifier
          not used by any other Session currently in the Server [
         */
        PacketProperty.ASSIGNED_CLIENT_IDENTIFIER,
        /*
          Followed by the Two Byte Integer representing the Topic Alias Maximum value. It is a Protocol Error to
          include the Topic Alias Maximum value more than once. If the Topic Alias Maximum property is absent,
          the default value is 0.

          This value indicates the highest value that the Server will accept as a Topic Alias sent by the Client. The
          Server uses this value to limit the number of Topic Aliases that it is willing to hold on this Connection. The
          Client MUST NOT send a Topic Alias in a PUBLISH packet to the Server greater than this value
          [MQTT1296 3.2.2-17]. A value of 0 indicates that the Server does not accept any Topic Aliases
          on this connection. If Topic Alias Maximum is absent or 0, the Client MUST NOT send any Topic Aliases on
          to the Server
         */
        PacketProperty.TOPIC_ALIAS_MAXIMUM,
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
          Client.

          The Server uses this value to give additional information to the Client. The Server MUST NOT send this
          property if it would increase the size of the CONNACK packet beyond the Maximum Packet Size specified
          by the Client [MQTT-3.2.2-19]. It is a Protocol Error to include the Reason String more than once.
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by a UTF-8 String Pair. This property can be used to provide additional information to the Client
          including diagnostic information. The Server MUST NOT send this property if it would increase the size of
          the CONNACK packet beyond the Maximum Packet Size specified by the Client [MQTT-3.2.2-20]. The
          User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
          name is allowed to appear more than once.

          The content and meaning of this property is not defined by this specification. The receiver of a CONNACK
          containing this property MAY ignore it.
         */
        PacketProperty.USER_PROPERTY,
        /*
          Followed by a Byte field. If present, this byte declares whether the Server supports Wildcard
          Subscriptions. A value is 0 means that Wildcard Subscriptions are not supported. A value of 1 means
          Wildcard Subscriptions are supported. If not present, then Wildcard Subscriptions are supported. It is a
          Protocol Error to include the Wildcard Subscription Available more than once or to send a value other
          than 0 or 1.

          Standards Track Work Product Copyright © OASIS Open 2019. All Rights Reserved. Page 51 of 137

          If the Server receives a SUBSCRIBE packet containing a Wildcard Subscription and it does not support
          Wildcard Subscriptions, this is a Protocol Error. The Server uses DISCONNECT with Reason Code 0xA2
          (Wildcard Subscriptions not supported) as described in section 4.13.

          If a Server supports Wildcard Subscriptions, it can still reject a particular subscribe request containing a
          Wildcard Subscription. In this case the Server MAY send a SUBACK Control Packet with a Reason Code
          0xA2 (Wildcard Subscriptions not supported).
         */
        PacketProperty.WILDCARD_SUBSCRIPTION_AVAILABLE,
        /*
          Followed by a Byte field. If present, this byte declares whether the Server supports Subscription
          Identifiers. A value is 0 means that Subscription Identifiers are not supported. A value of 1 means
          Subscription Identifiers are supported. If not present, then Subscription Identifiers are supported. It is a
          Protocol Error to include the Subscription Identifier Available more than once, or to send a value other
          than 0 or 1.

          If the Server receives a SUBSCRIBE packet containing Subscription Identifier and it does not support
          Subscription Identifiers, this is a Protocol Error. The Server uses DISCONNECT with Reason Code of
          0xA1 (Subscription Identifiers not supported) as described in section 4.13.
         */
        PacketProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE,
        /*
          Followed by a Byte field. If present, this byte declares whether the Server supports Shared Subscriptions.
          A value is 0 means that Shared Subscriptions are not supported. A value of 1 means Shared
          Subscriptions are supported. If not present, then Shared Subscriptions are supported. It is a Protocol
          Error to include the Shared Subscription Available more than once or to send a value other than 0 or 1.

          If the Server receives a SUBSCRIBE packet containing Shared Subscriptions and it does not support
          Shared Subscriptions, this is a Protocol Error. The Server uses DISCONNECT with Reason Code 0x9E
          (Shared Subscriptions not supported) as described in section 4.13.
         */
        PacketProperty.SHARED_SUBSCRIPTION_AVAILABLE,
        /*
          Followed by a Two Byte Integer with the Keep Alive time assigned by the Server. If the Server sends a
          Server Keep Alive on the CONNACK packet, the Client MUST use this value instead of the Keep Alive
          value the Client sent on CONNECT [MQTT-3.2.2-21]. If the Server does not send the Server Keep Alive,
          the Server MUST use the Keep Alive value set by the Client on CONNECT [MQTT-3.2.2-22]. It is a
          Protocol Error to include the Server Keep Alive more than once.
         */
        PacketProperty.SERVER_KEEP_ALIVE,
        /*
          Followed by a UTF-8 Encoded String which is used as the basis for creating a Response Topic. The way
          in which the Client creates a Response Topic from the Response Information is not defined by this
          specification. It is a Protocol Error to include the Response Information more than once.

          If the Client sends a Request Response Information with a value 1, it is OPTIONAL for the Server to send
          the Response Information in the CONNACK.
         */
        PacketProperty.RESPONSE_INFORMATION,
        /*
          Followed by a UTF-8 Encoded String which can be used by the Client to identify another Server to use. It
          is a Protocol Error to include the Server Reference more than once.

          The Server uses a Server Reference in either a CONNACK or DISCONNECT packet with Reason code
          of 0x9C (Use another server) or Reason Code 0x9D (Server moved) as described in section 4.13.

          Refer to section 4.11 Server redirection for information about how Server Reference is used
         */
        PacketProperty.SERVER_REFERENCE,
        /*
          Followed by a UTF-8 Encoded String containing the name of the authentication method. It is a Protocol
          Error to include the Authentication Method more than once. Refer to section 4.12 for more information
          about extended authentication.
         */
        PacketProperty.AUTHENTICATION_METHOD,
        /*
          Followed by Binary Data containing authentication data. The contents of this data are defined by the
          authentication method and the state of already exchanged authentication data. It is a Protocol Error to
          include the Authentication Data more than once. Refer to section 4.12 for more information about
          extended authentication.
         */
        PacketProperty.AUTHENTICATION_DATA
    );

    /**
     * The values the Connect Reason Code are shown below. If a well formed CONNECT packet is received
     * by the Server, but the Server is unable to complete the Connection the Server MAY send a CONNACK
     * packet containing the appropriate Connect Reason code from this table. If a Server sends a CONNACK
     * packet containing a Reason code of 128 or greater it MUST then close the Network Connection
     */
    private @NotNull ConnectAckReasonCode reasonCode;
    private @NotNull QoS maximumQos;

    /**
     * The Session Present flag informs the Client whether the Server is using Session State from a
     * previous connection for this ClientID.
     * This allows the Client and Server to have a consistent view of the Session State.
     * If the Server accepts a connection with Clean Start set to 1, the Server MUST set Session
     * Present to 0 in the CONNACK packet in addition to setting a 0x00 (Success) Reason Code in the CONNACK packet
     */
    private boolean sessionPresent;

    // properties
    private @NotNull String assignedClientId;
    private @NotNull String reason;
    private @NotNull String responseInformation;
    private @NotNull String authenticationMethod;
    private @NotNull String serverReference;
    private @NotNull byte[] authenticationData;

    private long sessionExpiryInterval;

    private int receiveMax;
    private int maximumPacketSize;
    private int topicAliasMaximum;
    private int serverKeepAlive;

    private boolean retainAvailable;
    private boolean wildcardSubscriptionAvailable;
    private boolean sharedSubscriptionAvailable;
    private boolean subscriptionIdAvailable;

    public ConnectAckInPacket(byte info) {
        super(info);
        this.userProperties = Array.empty();
        this.reasonCode = ConnectAckReasonCode.SUCCESS;
        this.maximumQos = QoS.EXACTLY_ONCE_DELIVERY;
        this.retainAvailable = MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT;
        this.assignedClientId = StringUtils.EMPTY;
        this.reason = StringUtils.EMPTY;
        this.sharedSubscriptionAvailable = MqttPropertyConstants.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT;
        this.wildcardSubscriptionAvailable = MqttPropertyConstants.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT;
        this.subscriptionIdAvailable = MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT;
        this.responseInformation = StringUtils.EMPTY;
        this.serverReference = StringUtils.EMPTY;
        this.authenticationMethod = StringUtils.EMPTY;
        this.authenticationData = ArrayUtils.EMPTY_BYTE_ARRAY;
        this.serverKeepAlive = MqttPropertyConstants.SERVER_KEEP_ALIVE_UNDEFINED;
        this.maximumPacketSize = MqttPropertyConstants.MAXIMUM_PACKET_SIZE_UNDEFINED;
        this.sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED;
        this.topicAliasMaximum = MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_UNDEFINED;
        this.receiveMax = MqttPropertyConstants.RECEIVE_MAXIMUM_UNDEFINED;
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {

        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718035
        sessionPresent = readUnsignedByte(buffer) == 1;
        reasonCode = ConnectAckReasonCode.of(connection.isSupported(MqttVersion.MQTT_5), readUnsignedByte(buffer));
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull byte[] value) {
        switch (property) {
            case AUTHENTICATION_DATA:
                authenticationData = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
        switch (property) {
            case REASON_STRING:
                reason = value;
                break;
            case ASSIGNED_CLIENT_IDENTIFIER:
                assignedClientId = value;
                break;
            case RESPONSE_INFORMATION:
                responseInformation = value;
                break;
            case AUTHENTICATION_METHOD:
                authenticationMethod = value;
                break;
            case SERVER_REFERENCE:
                serverReference = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, long value) {
        switch (property) {
            case WILDCARD_SUBSCRIPTION_AVAILABLE:
                wildcardSubscriptionAvailable = NumberUtils.toBoolean(value);
                break;
            case SHARED_SUBSCRIPTION_AVAILABLE:
                sharedSubscriptionAvailable = NumberUtils.toBoolean(value);
                break;
            case SUBSCRIPTION_IDENTIFIER_AVAILABLE:
                subscriptionIdAvailable = NumberUtils.toBoolean(value);
                break;
            case RETAIN_AVAILABLE:
                retainAvailable = NumberUtils.toBoolean(value);
                break;
            case RECEIVE_MAXIMUM:
                receiveMax = (int) NumberUtils.validate(
                    value,
                    MqttPropertyConstants.RECEIVE_MAXIMUM_MIN,
                    MqttPropertyConstants.RECEIVE_MAXIMUM_MAX
                );
                break;
            case MAXIMUM_QOS:
                maximumQos = QoS.of((int) value);
                break;
            case SERVER_KEEP_ALIVE:
                serverKeepAlive = NumberUtils.validate(
                    (int) value,
                    MqttPropertyConstants.SERVER_KEEP_ALIVE_MIN,
                    MqttPropertyConstants.SERVER_KEEP_ALIVE_MAX
                );
                break;
            case TOPIC_ALIAS_MAXIMUM:
                topicAliasMaximum = NumberUtils.validate(
                    (int) value,
                    MqttPropertyConstants.TOPIC_ALIAS_MIN,
                    MqttPropertyConstants.TOPIC_ALIAS_MAX
                );
                break;
            case SESSION_EXPIRY_INTERVAL:
                sessionExpiryInterval = NumberUtils.validate(
                    value,
                    MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_MIN,
                    MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_INFINITY
                );
                break;
            case MAXIMUM_PACKET_SIZE:
                maximumPacketSize = NumberUtils.validate(
                    (int) value,
                    MqttPropertyConstants.MAXIMUM_PACKET_SIZE_MIN,
                    MqttPropertyConstants.MAXIMUM_PACKET_SIZE_MAX
                );
                break;
            default:
                unexpectedProperty(property);
        }
    }
}
