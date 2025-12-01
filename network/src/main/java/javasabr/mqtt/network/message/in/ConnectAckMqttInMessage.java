package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.util.MqttDataUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Acknowledge connection request.
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectAckMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_FLAGS = 0b0000_0000;
  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.CONNECT_ACK.ordinal();

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the Four Byte Integer representing the Session Expiry Interval in seconds. It is a Protocol
        Error to include the Session Expiry Interval more than once.

        If the Session Expiry Interval is absent the value in the CONNECT Packet used. The server uses this
        property to inform the Client that it is using a value other than that sent by the Client in the CONNACK.
       */
      MqttMessageProperty.SESSION_EXPIRY_INTERVAL,
      /*
        Followed by the Two Byte Integer representing the Receive Maximum value. It is a Protocol Error to
        include the Receive Maximum value more than once or for it to have the value 0.

        The Server uses this value to limit the number of QoS 1 and QoS 2 publications that it is willing to
        process concurrently for the Client. It does not provide a mechanism to limit the QoS 0 publications that
        the Client might try to send.

        If the Receive Maximum value is absent, then its value defaults to 65,535.
       */
      MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES,
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
      MqttMessageProperty.MAXIMUM_QOS,
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
      MqttMessageProperty.RETAIN_AVAILABLE,
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
      MqttMessageProperty.MAXIMUM_MESSAGE_SIZE,
      /*
        Followed by the UTF-8 string which is the Assigned Client Identifier. It is a Protocol Error to include the
        Assigned Client Identifier more than once.

        The Client Identifier which was assigned by the Server because a zero length Client Identifier was found
        in the CONNECT packet.

        If the Client connects using a zero length Client Identifier, the Server MUST respond with a CONNACK
        containing an Assigned Client Identifier. The Assigned Client Identifier MUST be a new Client Identifier
        not used by any other Session currently in the Server [
       */
      MqttMessageProperty.ASSIGNED_CLIENT_IDENTIFIER,
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
      MqttMessageProperty.TOPIC_ALIAS_MAXIMUM,
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
        Client.

        The Server uses this value to give additional information to the Client. The Server MUST NOT send this
        property if it would increase the size of the CONNACK packet beyond the Maximum Packet Size specified
        by the Client [MQTT-3.2.2-19]. It is a Protocol Error to include the Reason String more than once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by a UTF-8 String Pair. This property can be used to provide additional information to the Client
        including diagnostic information. The Server MUST NOT send this property if it would increase the size of
        the CONNACK packet beyond the Maximum Packet Size specified by the Client [MQTT-3.2.2-20]. The
        User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once.

        The content and meaning of this property is not defined by this specification. The receiver of a CONNACK
        containing this property MAY ignore it.
       */
      MqttMessageProperty.USER_PROPERTY,
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
      MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE,
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
      MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE,
      /*
        Followed by a Byte field. If present, this byte declares whether the Server supports Shared Subscriptions.
        A value is 0 means that Shared Subscriptions are not supported. A value of 1 means Shared
        Subscriptions are supported. If not present, then Shared Subscriptions are supported. It is a Protocol
        Error to include the Shared Subscription Available more than once or to send a value other than 0 or 1.

        If the Server receives a SUBSCRIBE packet containing Shared Subscriptions and it does not support
        Shared Subscriptions, this is a Protocol Error. The Server uses DISCONNECT with Reason Code 0x9E
        (Shared Subscriptions not supported) as described in section 4.13.
       */
      MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE,
      /*
        Followed by a Two Byte Integer with the Keep Alive time assigned by the Server. If the Server sends a
        Server Keep Alive on the CONNACK packet, the Client MUST use this value instead of the Keep Alive
        value the Client sent on CONNECT [MQTT-3.2.2-21]. If the Server does not send the Server Keep Alive,
        the Server MUST use the Keep Alive value set by the Client on CONNECT [MQTT-3.2.2-22]. It is a
        Protocol Error to include the Server Keep Alive more than once.
       */
      MqttMessageProperty.SERVER_KEEP_ALIVE,
      /*
        Followed by a UTF-8 Encoded String which is used as the basis for creating a Response Topic. The way
        in which the Client creates a Response Topic from the Response Information is not defined by this
        specification. It is a Protocol Error to include the Response Information more than once.

        If the Client sends a Request Response Information with a value 1, it is OPTIONAL for the Server to send
        the Response Information in the CONNACK.
       */
      MqttMessageProperty.RESPONSE_INFORMATION,
      /*
        Followed by a UTF-8 Encoded String which can be used by the Client to identify another Server to use. It
        is a Protocol Error to include the Server Reference more than once.

        The Server uses a Server Reference in either a CONNACK or DISCONNECT packet with Reason code
        of 0x9C (Use another server) or Reason Code 0x9D (Server moved) as described in section 4.13.

        Refer to section 4.11 Server redirection for information about how Server Reference is used
       */
      MqttMessageProperty.SERVER_REFERENCE,
      /*
        Followed by a UTF-8 Encoded String containing the name of the authentication method. It is a Protocol
        Error to include the Authentication Method more than once. Refer to section 4.12 for more information
        about extended authentication.
       */
      MqttMessageProperty.AUTHENTICATION_METHOD,
      /*
        Followed by Binary Data containing authentication data. The contents of this data are defined by the
        authentication method and the state of already exchanged authentication data. It is a Protocol Error to
        include the Authentication Data more than once. Refer to section 4.12 for more information about
        extended authentication.
       */
      MqttMessageProperty.AUTHENTICATION_DATA);

  /**
   * The values the Connect Reason Code are shown below. If a well formed CONNECT packet is received by the Server, but
   * the Server is unable to complete the Connection the Server MAY send a CONNACK packet containing the appropriate
   * Connect Reason code from this table. If a Server sends a CONNACK packet containing a Reason code of 128 or greater
   * it MUST then close the Network Connection
   */
  ConnectAckReasonCode reasonCode;

  @Nullable
  QoS maxQos;

  /**
   * The Session Present flag informs the Client whether the Server is using Session State from a previous connection
   * for this ClientID. This allows the Client and Server to have a consistent view of the Session State. If the Server
   * accepts a connection with Clean Start set to 1, the Server MUST set Session Present to 0 in the CONNACK packet in
   * addition to setting a 0x00 (Success) Reason Code in the CONNACK packet
   */
  boolean sessionPresent;

  // properties
  @Nullable
  String assignedClientId;
  @Nullable
  String reason;
  @Nullable
  String responseInformation;
  @Nullable
  String serverReference;
  @Nullable
  String authenticationMethod;
  byte @Nullable [] authenticationData;

  long sessionExpiryInterval;

  int receiveMaxPublishes;
  int maxMessageSize;
  int topicAliasMaxValue;
  int serverKeepAlive;

  int retainAvailable;
  int wildcardSubscriptionAvailable;
  int subscriptionIdAvailable;
  int sharedSubscriptionAvailable;

  public ConnectAckMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = ConnectAckReasonCode.SUCCESS;
    this.sessionExpiryInterval = MqttProperties.SESSION_EXPIRY_INTERVAL_IS_NOT_SET;
    this.receiveMaxPublishes = MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET;
    this.retainAvailable = MqttProperties.RETAIN_AVAILABLE_IS_NOT_SET;
    this.maxMessageSize = MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET;
    this.topicAliasMaxValue = MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET;
    this.wildcardSubscriptionAvailable = MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_IS_NOT_SET;
    this.subscriptionIdAvailable = MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_IS_NOT_SET;
    this.sharedSubscriptionAvailable = MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_IS_NOT_SET;
    this.serverKeepAlive = MqttProperties.SERVER_KEEP_ALIVE_IS_NOT_SET;
  }

  @Override
  public byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.CONNECT_ACK;
  }

  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == MESSAGE_FLAGS;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718035
    int connectAckFlags = readByteUnsigned(buffer);
    sessionPresent = (connectAckFlags & 0b0000_0001) != 0;
    reasonCode = ConnectAckReasonCode.ofCode(connection.isSupported(MqttVersion.MQTT_5), readByteUnsigned(buffer));
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, byte[] value) {
    switch (property) {
      case AUTHENTICATION_DATA -> {
        if (authenticationData != null) {
          alreadyPresentedProperty(property);
        }
        authenticationData = value;
      }
      default -> unsupportedProperty(property);
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case ASSIGNED_CLIENT_IDENTIFIER ->{
        if (assignedClientId != null) {
          alreadyPresentedProperty(property);
        }
        assignedClientId = value;
      }
      case REASON_STRING -> {
        if (reason != null) {
          alreadyPresentedProperty(property);
        }
        reason = value;
      }
      case RESPONSE_INFORMATION -> {
        if (responseInformation != null) {
          alreadyPresentedProperty(property);
        }
        responseInformation = value;
      }
      case AUTHENTICATION_METHOD -> {
        if (authenticationMethod != null) {
          alreadyPresentedProperty(property);
        }
        authenticationMethod = value;
      }
      case SERVER_REFERENCE -> {
        if (serverReference != null) {
          alreadyPresentedProperty(property);
        }
        serverReference = value;
      }
      default -> unsupportedProperty(property);
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, long value) {
    switch (property) {
      case WILDCARD_SUBSCRIPTION_AVAILABLE -> {
        if (wildcardSubscriptionAvailable != MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (!MqttDataUtils.isValidBoolean(value)) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_WILDCARD_SUBSCRIPTION_AVAILABLE);
        }
        wildcardSubscriptionAvailable = (int) value;
      }
      case SHARED_SUBSCRIPTION_AVAILABLE -> {
        if (sharedSubscriptionAvailable != MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (!MqttDataUtils.isValidBoolean(value)) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_SHARED_SUBSCRIPTION_AVAILABLE);
        }
        sharedSubscriptionAvailable = (int) value;
      }
      case SUBSCRIPTION_IDENTIFIER_AVAILABLE -> {
        if (subscriptionIdAvailable != MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (!MqttDataUtils.isValidBoolean(value)) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_SUBSCRIPTION_IDENTIFIERS_AVAILABLE);
        }
        subscriptionIdAvailable = (int) value;
      }
      case RETAIN_AVAILABLE -> {
        if (retainAvailable != MqttProperties.RETAIN_AVAILABLE_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (!MqttDataUtils.isValidBoolean(value)) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_RETAIN_AVAILABLE);
        }
        retainAvailable = (int) value;
      }
      case RECEIVE_MAXIMUM_PUBLISHES -> {
        if (receiveMaxPublishes != MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (value < MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_MIN
            || value > MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_MAX) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_RECEIVED_MAX_PUBLISHES);
        }
        receiveMaxPublishes = (int) value;
      }
      case MAXIMUM_QOS -> {
        if (maxQos != null) {
          alreadyPresentedProperty(property);
        } else if (value < QoS.AT_LEAST_ONCE.level() || value > QoS.EXACTLY_ONCE.level()) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_MAX_QOS);
        }
        maxQos = QoS.ofCode((int) value);
      }
      case SERVER_KEEP_ALIVE -> {
        if (serverKeepAlive != MqttProperties.SERVER_KEEP_ALIVE_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (value < MqttProperties.SERVER_KEEP_ALIVE_MIN || value > MqttProperties.SERVER_KEEP_ALIVE_MAX) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_SERVER_KEEP_ALIVE);
        }
        serverKeepAlive = (int) value;
      }
      case TOPIC_ALIAS_MAXIMUM -> {
        if (topicAliasMaxValue != MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (value > MqttProperties.TOPIC_ALIAS_MAX) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_TOPIC_ALIAS_MAX);
        }
        topicAliasMaxValue = (int) value;
      }
      case SESSION_EXPIRY_INTERVAL -> {
        if (sessionExpiryInterval != MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (value < MqttProperties.SESSION_EXPIRY_INTERVAL_MIN
            || value > MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_SESSION_EXPIRY_INTERVAL);
        }
        sessionExpiryInterval = value;
      }
      case MAXIMUM_MESSAGE_SIZE -> {
        if (maxMessageSize != MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET) {
          alreadyPresentedProperty(property);
        } else if (value < MqttProperties.MAXIMUM_MESSAGE_SIZE_MIN
            || value > MqttProperties.MAXIMUM_MESSAGE_SIZE_MAX) {
          throw new MalformedProtocolMqttException(MqttProtocolErrors.PROVIDED_INVALID_MAX_MESSAGE_SIZE);
        }
        maxMessageSize = (int) value;
      }
      default -> unsupportedProperty(property);
    }
  }
}
