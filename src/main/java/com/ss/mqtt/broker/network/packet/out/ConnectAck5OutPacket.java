package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Connect acknowledgment.
 */
public class ConnectAck5OutPacket extends ConnectAck311OutPacket {

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
        PacketProperty.TOPIC_ALIAS,
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

    private final @NotNull Array<StringPair> userProperties;

    private final @NotNull String requestedClientId;
    private final @NotNull String reason;
    private final @NotNull String serverReference;
    private final @NotNull String responseInformation;
    private final @NotNull String authenticationMethod;
    private final @NotNull byte[] authenticationData;

    private final long requestedSessionExpiryInterval;
    private final int requestedKeepAlive;

    public ConnectAck5OutPacket(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent,
        @NotNull String requestedClientId,
        long requestedSessionExpiryInterval,
        int requestedKeepAlive,
        @NotNull String reason,
        @NotNull String serverReference,
        @NotNull String responseInformation,
        @NotNull String authenticationMethod,
        @NotNull byte[] authenticationData,
        @NotNull Array<StringPair> userProperties
    ) {
        super(client, reasonCode, sessionPresent);
        this.requestedClientId = requestedClientId;
        this.requestedSessionExpiryInterval = requestedSessionExpiryInterval;
        this.requestedKeepAlive = requestedKeepAlive;
        this.reason = reason;
        this.serverReference = serverReference;
        this.responseInformation = responseInformation;
        this.authenticationMethod = authenticationMethod;
        this.authenticationData = authenticationData;
        this.userProperties = userProperties;
    }

    @Override
    public int getExpectedLength() {
        return -1;
    }

    @Override
    protected byte getReasonCodeValue() {
        return reasonCode.getMqtt5();
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        var connection = client.getConnection();
        var config = connection.getConfig();

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901080
        writeNotEmptyProperty(buffer, PacketProperty.REASON_STRING, reason);
        writeNotEmptyProperty(buffer, PacketProperty.RESPONSE_INFORMATION, responseInformation);
        writeNotEmptyProperty(buffer, PacketProperty.SERVER_REFERENCE, serverReference);
        writeNotEmptyProperty(buffer, PacketProperty.AUTHENTICATION_METHOD, authenticationMethod);
        writeNotEmptyProperty(buffer, PacketProperty.AUTHENTICATION_DATA, authenticationData);
        writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
        writeProperty(
            buffer,
            PacketProperty.MAXIMUM_QOS,
            config.getMaxQos().ordinal(),
            MqttPropertyConstants.MAXIMUM_QOS_DEFAULT.ordinal()
        );
        writeProperty(
            buffer,
            PacketProperty.RETAIN_AVAILABLE,
            config.isRetainAvailable(),
            MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.SESSION_EXPIRY_INTERVAL,
            requestedSessionExpiryInterval,
            client.getSessionExpiryInterval()
        );
        writeProperty(
            buffer,
            PacketProperty.ASSIGNED_CLIENT_IDENTIFIER,
            requestedClientId,
            client.getClientId()
        );
        writeProperty(
            buffer,
            PacketProperty.RECEIVE_MAXIMUM,
            client.getReceiveMax(),
            MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.MAXIMUM_PACKET_SIZE,
            client.getMaximumPacketSize(),
            MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.TOPIC_ALIAS_MAXIMUM,
            client.getTopicAliasMaximum(),
            MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.WILDCARD_SUBSCRIPTION_AVAILABLE,
            config.isWildcardSubscriptionAvailable(),
            MqttPropertyConstants.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE,
            config.isSubscriptionIdAvailable(),
            MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE
        );
        writeProperty(
            buffer,
            PacketProperty.SHARED_SUBSCRIPTION_AVAILABLE,
            config.isSharedSubscriptionAvailable(),
            MqttPropertyConstants.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
        );
        writeProperty(
            buffer,
            PacketProperty.SERVER_KEEP_ALIVE,
            requestedKeepAlive,
            client.getKeepAlive()
        );
    }
}
