package javasabr.mqtt.model.reason.code;

import javasabr.mqtt.model.NumberedEnumLookup;
import javasabr.rlib.common.util.NumberedEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DisconnectReasonCode implements NumberedEnum<DisconnectReasonCode> {
  /**
   * Close the connection normally. Do not send the Will Message. Client or Server.
   */
  NORMAL_DISCONNECTION(0x00),
  /**
   * The Client wishes to disconnect but requires that the Server also publishes its Will Message. Client.
   */
  DISCONNECT_WITH_WILL_MESSAGE(0x04),

  // ERRORS

  /**
   * The Connection is closed but the sender either does not wish to reveal the reason, or none of the other Reason
   * Codes apply. Client or Server.
   */
  UNSPECIFIED_ERROR(0x80),
  /**
   * The received packet does not conform to this specification. Client or Server.
   */
  MALFORMED_PACKET(0x81),
  /**
   * An unexpected or out of order packet was received. Client or Server.
   */
  PROTOCOL_ERROR(0x82),
  /**
   * The packet received is valid but cannot be processed by this implementation. Client or Server.
   */
  IMPLEMENTATION_SPECIFIC_ERROR(0x83),
  /**
   * The request is not authorized. Server.
   */
  NOT_AUTHORIZED(0x87),
  /**
   * The Server is busy and cannot continue processing requests from this Client. Server.
   */
  SERVER_BUSY(0x89),
  /**
   * The Server is shutting down. Server.
   */
  SERVER_SHUTTING_DOWN(0x8B),
  /**
   * The Connection is closed because no packet has been received for 1.5 times the Keepalive time. Server.
   */
  KEEP_ALIVE_TIMEOUT(0x8D),
  /**
   * Another Connection using the same ClientID has connected causing this Connection to be closed. Server.
   */
  SESSION_TAKEN_OVER(0x8E),
  /**
   * The Topic Filter is correctly formed, but is not accepted by this Sever. Server.
   */
  TOPIC_FILTER_INVALID(0x8F),
  /**
   * The Topic Name is correctly formed, but is not accepted by this Client or Server. Client or Server.
   */
  TOPIC_NAME_INVALID(0x90),
  /**
   * The Client or Server has received more than Receive Maximum publication for which it has not sent PUBACK or
   * PUBCOMP. Client or Server.
   */
  RECEIVE_MAXIMUM_EXCEEDED(0x93),
  /**
   * The Client or Server has received a PUBLISH packet containing a Topic Alias which is greater than the Maximum Topic
   * Alias it sent in the CONNECT or CONNACK packet. Client or Server.
   */
  TOPIC_ALIAS_INVALID(0x94),
  /**
   * The packet size is greater than Maximum Packet Size for this Client or Server. Client or Server.
   */
  PACKET_TOO_LARGE(0x95),
  /**
   * The received data rate is too high. Client or Server.
   */
  MESSAGE_RATE_TOO_HIGH(0x96),
  /**
   * An implementation or administrative imposed limit has been exceeded. Client or Server.
   */
  QUOTA_EXCEEDED(0x97),
  /**
   * The Connection is closed due to an administrative action. Client or Server.
   */
  ADMINISTRATIVE_ACTION(0x98),
  /**
   * The payload format does not match the one specified by the Payload Format Indicator. Client or Server.
   */
  PAYLOAD_FORMAT_INVALID(0x99),
  /**
   * The Server has does not support retained messages. Server.
   */
  RETAIN_NOT_SUPPORTED(0x9A),
  /**
   * The Client specified a QoS greater than the QoS specified in a Maximum QoS in the CONNACK. Server.
   */
  QOS_NOT_SUPPORTED(0x9B),
  /**
   * The Client should temporarily change its Server. Server.
   */
  USE_ANOTHER_SERVER(0x9C),
  /**
   * The Server is moved and the Client should permanently change its server location. Server.
   */
  SERVER_MOVED(0x9D),
  /**
   * The Server does not support Shared Subscriptions. Server.
   */
  SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(0x9E),
  /**
   * This connection is closed because the connection rate is too high. Server.
   */
  CONNECTION_RATE_EXCEEDED(0x9F),
  /**
   * The maximum connection time authorized for this connection has been exceeded. Server.
   */
  MAXIMUM_CONNECT_TIME(0xA0),
  /**
   * The Server does not support Subscription Identifiers; the subscription is not accepted. Server.
   */
  SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(0xA1),
  /**
   * The Server does not support Wildcard Subscriptions; the subscription is not accepted. Server.
   */
  WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(0xA2);

  private static final NumberedEnumLookup<DisconnectReasonCode> NUMBERED_MAP =
      new NumberedEnumLookup<>(DisconnectReasonCode.values());

  public static DisconnectReasonCode ofCode(int code) {
    return NUMBERED_MAP.require(code);
  }

  int code;

  @Override
  public int number() {
    return code;
  }
}
