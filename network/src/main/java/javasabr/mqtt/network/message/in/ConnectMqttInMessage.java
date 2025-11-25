package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.exception.ConnectionRejectException;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.NumberUtils;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Connection request.
 */
@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectMqttInMessage extends MqttInMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.CONNECT.ordinal();

  static {
    DebugUtils.registerIncludedFields("clientId", "keepAlive", "cleanStart", "mqttVersion");
  }

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        If the Session Expiry Interval is absent the value 0 is used. If it is set to 0,
        or is absent, the Session ends when the Network Connection is closed.
        If the Session Expiry Interval is 0xFFFFFFFF (UINT_MAX), the Session does not expire.

        The Client and Server MUST store the Session State after the Network Connection is closed if the
        Session Expiry Interval is greater than 0
       */
      MqttMessageProperty.SESSION_EXPIRY_INTERVAL,
      /*
        Followed by the Two Byte Integer representing the Receive Maximum value. It is a Protocol Error to
        include the Receive Maximum value more than once or for it to have the value 0.

        The Client uses this value to limit the number of QoS 1 and QoS 2 publications that it is willing to process
        concurrently. There is no mechanism to limit the QoS 0 publications that the Server might try to send.

        The value of Receive Maximum applies only to the current Network Connection. If the Receive Maximum
        value is absent then its value defaults to 65,535.
       */
      MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES,
      /*
        Followed by a Four Byte Integer representing the Maximum Packet Size the Client is willing to accept. If
        the Maximum Packet Size is not present, no limit on the packet size is imposed beyond the limitations in
        the protocol as a result of the remaining length encoding and the protocol header sizes.

        It is a Protocol Error to include the Maximum Packet Size more than once, or for the value to be set to
        zero.
       */
      MqttMessageProperty.MAXIMUM_MESSAGE_SIZE,
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
      MqttMessageProperty.TOPIC_ALIAS_MAXIMUM,
      /*
        Followed by a Byte with a value of either 0 or 1. It is Protocol Error to include the Request Response
        Information more than once, or to have a value other than 0 or 1. If the Request Response Information is
        absent, the value of 0 is used.

        The Client uses this value to request the Server to return Response Information in the CONNACK. A
        value of 0 indicates that the Server MUST NOT return Response Information [MQTT-3.1.2-28]. If the
        value is 1 the Server MAY return Response Information in the CONNACK packet.
       */
      MqttMessageProperty.REQUEST_RESPONSE_INFORMATION,
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
      MqttMessageProperty.REQUEST_PROBLEM_INFORMATION,
      /*
        The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once
       */
      MqttMessageProperty.USER_PROPERTY,
      /*
        Followed by a UTF-8 Encoded String containing the name of the authentication method used for
        extended authentication .It is a Protocol Error to include Authentication Method more than once.
        If Authentication Method is absent, extended authentication is not performed. Refer to section 4.12.

        If a Client sets an Authentication Method in the CONNECT, the Client MUST NOT send any packets other
        than AUTH or DISCONNECT packets until it has received a CONNACK packet
       */
      MqttMessageProperty.AUTHENTICATION_METHOD,
      /*
        Followed by Binary Data containing authentication data. It is a Protocol Error to include Authentication
        Data if there is no Authentication Method. It is a Protocol Error to include Authentication Data more than
        once.

        The contents of this data are defined by the authentication method. Refer to section 4.12 for more
        information about extended authentication.
       */
      MqttMessageProperty.AUTHENTICATION_DATA);

  private static final Set<MqttMessageProperty> WILL_PROPERTIES = EnumSet.of(
      /*
        Followed by the Four Byte Integer representing the Will Delay Interval in seconds. It is a Protocol Error to
        include the Will Delay Interval more than once. If the Will Delay Interval is absent, the default value is 0
        and there is no delay before the Will Message is published.

        The Server delays publishing the Client’s Will Message until the Will Delay Interval has passed or the
        Session ends, whichever happens first. If a new Network Connection to this Session is made before the
        Will Delay Interval has passed, the Server MUST NOT send the Will Message
       */
      MqttMessageProperty.WILL_DELAY_INTERVAL,
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
      MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR,
      /*
        Followed by the Four Byte Integer representing the Message Expiry Interval. It is a Protocol Error to
        include the Message Expiry Interval more than once.

        If present, the Four Byte value is the lifetime of the Will Message in seconds and is sent as the
        Publication Expiry Interval when the Server publishes the Will Message.

        If absent, no Message Expiry Interval is sent when the Server publishes the Will Message.
       */
      MqttMessageProperty.MESSAGE_EXPIRY_INTERVAL,
      /*
        Followed by a UTF-8 Encoded String describing the content of the Will Message. It is a Protocol Error to
        include the Content Type more than once. The value of the Content Type is defined by the sending and
        receiving application.
       */
      MqttMessageProperty.CONTENT_TYPE,
      /*
        Followed by a UTF-8 Encoded String which is used as the Topic Name for a response message. It is a
        Protocol Error to include the Response Topic more than once. The presence of a Response Topic
        identifies the Will Message as a Request.
       */
      MqttMessageProperty.RESPONSE_TOPIC,
      /*
        Followed by Binary Data. The Correlation Data is used by the sender of the Request Message to identify
        which request the Response Message is for when it is received. It is a Protocol Error to include
        Correlation Data more than once. If the Correlation Data is not present, the Requester does not require
        any correlation data.

        The value of the Correlation Data only has meaning to the sender of the Request Message and receiver
        of the Response Message.
       */
      MqttMessageProperty.CORRELATION_DATA,
      /*
        Followed by a UTF-8 String Pair. The User Property is allowed to appear multiple times to represent
        multiple name, value pairs. The same name is allowed to appear more than once.

        The Server MUST maintain the order of User Properties when publishing the Will Message
       */
      MqttMessageProperty.USER_PROPERTY);

  MqttVersion mqttVersion = MqttVersion.MQTT_3_1_1;

  String clientId = StringUtils.EMPTY;
  String willTopic = StringUtils.EMPTY;
  byte[] willPayload = ArrayUtils.EMPTY_BYTE_ARRAY;

  String username = StringUtils.EMPTY;
  byte[] password = ArrayUtils.EMPTY_BYTE_ARRAY;

  int keepAlive;
  int willQos;
  boolean willRetain;
  boolean cleanStart;

  boolean hasUserName;
  boolean hasPassword;
  boolean willFlag;

  // properties
  String authenticationMethod = StringUtils.EMPTY;
  byte[] authenticationData = ArrayUtils.EMPTY_BYTE_ARRAY;

  long sessionExpiryInterval = MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED;
  int receiveMaxPublishes = MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_UNDEFINED;
  int maxPacketSize = MqttProperties.MAXIMUM_MESSAGE_SIZE_UNDEFINED;
  int topicAliasMaxValue = MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED;
  boolean requestResponseInformation = false;
  boolean requestProblemInformation = false;

  public ConnectMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    var connectionConfig = connection.serverConnectionConfig();

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718030
    String protocolName = readString(buffer, connectionConfig.maxStringLength());
    byte protocolLevel = buffer.get();

    mqttVersion = MqttVersion.of(protocolName, protocolLevel);
    if (mqttVersion == MqttVersion.UNKNOWN) {
      throw new ConnectionRejectException(ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION);
    }

    int flags = readByteUnsigned(buffer);
    /*
    Used to indicate whether the will message is a retained message.
     */
    willRetain = NumberUtils.isSetBit(flags, 5);
    /*
    Used to indicate the QoS of the will message.
     */
    willQos = (flags & 0x18) >> 3;
    /*
    Used to indicate whether the current connection is a new session or
    a continuation of an existing session, which determines whether the server will directly
    create a new session or attempt to reuse an existing session.
     */
    cleanStart = NumberUtils.isSetBit(flags, 1);

    // for mqtt 3.1.1+
    if (mqttVersion.ordinal() >= MqttVersion.MQTT_3_1_1.ordinal()) {
      boolean zeroReservedFlag = NumberUtils.isNotSetBit(flags, 0);
      if (!zeroReservedFlag) {
        /*
         The Server MUST validate that the reserved flag in the CONNECT packet is set to 0 [MQTT-3.1.2-3]. If
         the reserved flag is not 0 it is a Malformed Packet. Refer to section 4.13 for information about
         handling
         errors.
        */
        throw new ConnectionRejectException(ConnectAckReasonCode.MALFORMED_PACKET);
      }
    }

    hasUserName = NumberUtils.isSetBit(flags, 7);
    hasPassword = NumberUtils.isSetBit(flags, 6);

    // for mqtt < 5 we cannot have password without user
    if (mqttVersion.ordinal() < MqttVersion.MQTT_5.ordinal() && !hasUserName && hasPassword) {
      throw new ConnectionRejectException(ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD);
    }

    /*
    Used to indicate whether the Payload contains relevant fields of the will message.
     */
    willFlag = NumberUtils.isSetBit(flags, 2);

    /*
    This is a double-byte length unsigned integer used to indicate the maximum
    time interval between two adjacent control packets sent by the client.
     */
    keepAlive = readShortUnsigned(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    var connectionConfig = connection.serverConnectionConfig();
    /*
      The ClientID MUST be present and is the first field in the CONNECT packet Payload
      The ClientID MUST be a UTF-8 Encoded String as defined in

      The Server MUST allow ClientID’s which are between 1 and 23 UTF-8 encoded bytes in length, and that
      contain only the characters 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

      The Server MAY allow ClientID’s that contain more than 23 encoded bytes. The Server MAY allow
      ClientID’s that contain characters not included in the list given above.

      A Server MAY allow a Client to supply a ClientID that has a length of zero bytes, however if it does so the
      Server MUST treat this as a special case and assign a unique ClientID to that Client. It
      MUST then process the CONNECT packet as if the Client had provided that unique ClientID, and MUST
      return the Assigned Client Identifier in the CONNACK packet

      If the Server rejects the ClientID it MAY respond to the CONNECT packet with a CONNACK using
      Reason Code 0x85 (Client Identifier not valid) and then it MUST close the Network Connection
     */
    clientId = readString(buffer, Integer.MAX_VALUE);

    if (willFlag && mqttVersion.ordinal() >= MqttVersion.MQTT_5.ordinal()) {
      readProperties(connection, buffer, WILL_PROPERTIES);
    }

    if (willFlag) {
      willTopic = readString(buffer, Integer.MAX_VALUE);
    }

    if (willFlag) {
      willPayload = readBytes(buffer, connectionConfig.maxBinarySize());
    }

    if (hasUserName) {
      username = readString(buffer, Integer.MAX_VALUE);
    }

    if (hasPassword) {
      password = readBytes(buffer, connectionConfig.maxBinarySize());
    }
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    return mqttVersion.ordinal() >= MqttVersion.MQTT_5.ordinal();
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, byte[] value) {
    switch (property) {
      case AUTHENTICATION_DATA -> authenticationData = value;
      default -> unsupportedProperty(property);
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case AUTHENTICATION_METHOD -> authenticationMethod = value;
      default -> unsupportedProperty(property);
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, long value) {
    switch (property) {
      case REQUEST_RESPONSE_INFORMATION -> requestResponseInformation = NumberUtils.toBoolean(value);
      case REQUEST_PROBLEM_INFORMATION -> requestProblemInformation = NumberUtils.toBoolean(value);
      case RECEIVE_MAXIMUM_PUBLISHES -> receiveMaxPublishes = NumberUtils.validate(
          (int) value,
          MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_MIN,
          MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_MAX);
      case TOPIC_ALIAS_MAXIMUM -> topicAliasMaxValue = NumberUtils.validate(
          (int) value,
          MqttProperties.TOPIC_ALIAS_MIN,
          MqttProperties.TOPIC_ALIAS_MAX);
      case SESSION_EXPIRY_INTERVAL -> sessionExpiryInterval = NumberUtils.validate(
          value,
          MqttProperties.SESSION_EXPIRY_INTERVAL_MIN,
          MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY);
      case MAXIMUM_MESSAGE_SIZE -> maxPacketSize = NumberUtils.validate(
          (int) value,
          MqttProperties.MAXIMUM_MESSAGE_SIZE_MIN,
          MqttProperties.MAXIMUM_MESSAGE_SIZE_MAX);
      default -> unsupportedProperty(property);
    }
  }
}
