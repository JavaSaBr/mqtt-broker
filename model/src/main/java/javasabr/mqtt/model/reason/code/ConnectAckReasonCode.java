package javasabr.mqtt.model.reason.code;

import javasabr.mqtt.model.NumberedEnumLookup;
import javasabr.rlib.common.util.NumberedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors
@RequiredArgsConstructor
public enum ConnectAckReasonCode implements NumberedEnum<ConnectAckReasonCode> {
  /**
   * The Connection is accepted.
   */
  SUCCESS(0x00, 0x00),

  // WITH REASONS BELOW SERVER MUST CLOSE CONNECTION
  /**
   * The Server does not wish to reveal the reason for the failure, or none of the other Reason Codes apply.
   */
  UNSPECIFIED_ERROR(0x01, 0x80),
  /**
   * Data within the CONNECT packet could not be correctly parsed.
   */
  MALFORMED_PACKET(0x01, 0x81),
  /**
   * Data in the CONNECT packet does not conform to this specification.
   */
  PROTOCOL_ERROR(0x01, 0x82),
  /**
   * The CONNECT is valid but is not accepted by this Server.
   */
  IMPLEMENTATION_SPECIFIC_ERROR(0x01, 0x83),
  /**
   * The Server does not support the version of the MQTT protocol requested by the Client
   */
  UNSUPPORTED_PROTOCOL_VERSION(0x01, 0x84),
  /**
   * The Client Identifier is a valid string but is not allowed by the Server.
   */
  CLIENT_IDENTIFIER_NOT_VALID(0x02, 0x85),
  /**
   * The Server does not accept the User Name or Password specified by the Client
   */
  BAD_USER_NAME_OR_PASSWORD(0x04, 0x86),
  /**
   * The Client is not authorized to connect.
   */
  NOT_AUTHORIZED(0x05, 0x87),
  /**
   * The MQTT Server is not available
   */
  SERVER_UNAVAILABLE(0x03, 0x88),
  /**
   * The Server is busy. Try again later.
   */
  SERVER_BUSY(0x01, 0x89),
  /**
   * This Client has been banned by administrative action. Contact the server administrator.
   */
  BANNED(0x01, 0x8A),
  /**
   * The authentication method is not supported or does not match the authentication method currently in use.
   */
  BAD_AUTHENTICATION_METHOD(0x04, 0x8C),
  /**
   * The Will Topic Name is not malformed, but is not accepted by this Server.
   */
  TOPIC_NAME_INVALID(0x01, 0x90),
  /**
   * The CONNECT packet exceeded the maximum permissible size.
   */
  PACKET_TOO_LARGE(0x01, 0x95),
  /**
   * An implementation or administrative imposed limit has been exceeded.
   */
  QUOTA_EXCEEDED(0x01, 0x97),
  /**
   * The Will Payload does not match the specified Payload Format Indicator.
   */
  PAYLOAD_FORMAT_INVALID(0x01, 0x99),
  /**
   * The Server does not support retained messages, and Will Retain was set to 1.
   */
  RETAIN_NOT_SUPPORTED(0x01, 0x9A),
  /**
   * The Server does not support the QoS set in Will QoS.
   */
  QOS_NOT_SUPPORTED(0x01, 0x9B),
  /**
   * The Client should temporarily use another server.
   */
  USE_ANOTHER_SERVER(0x01, 0x9C),
  /**
   * The Client should permanently use another server.
   */
  SERVER_MOVED(0x01, 0x9D),
  /**
   * The connection rate limit has been exceeded.
   */
  CONNECTION_RATE_EXCEEDED(0x01, 0x9F);

  private static final NumberedEnumLookup<ConnectAckReasonCode> MQTT5_NUMBERED_MAP =
      new NumberedEnumLookup<>(ConnectAckReasonCode.values());

  public static ConnectAckReasonCode ofCode(boolean mqtt5, int reasonCode) {
    return mqtt5 ? ofMqtt5Code(reasonCode) : ofMqtt311Code(reasonCode);
  }

  public static ConnectAckReasonCode ofMqtt311Code(int code) {
    return switch (code) {
      case 0x00 -> SUCCESS;
      case 0x01 -> UNSUPPORTED_PROTOCOL_VERSION;
      case 0x02 -> CLIENT_IDENTIFIER_NOT_VALID;
      case 0x03 -> SERVER_UNAVAILABLE;
      case 0x04 -> BAD_USER_NAME_OR_PASSWORD;
      case 0x05 -> NOT_AUTHORIZED;
      default -> throw new IllegalArgumentException("Unsupported reason code: " + code);
    };
  }

  public static ConnectAckReasonCode ofMqtt5Code(int code) {
    return MQTT5_NUMBERED_MAP.require(code);
  }

  private final int mqtt311;
  private final int mqtt5;

  @Override
  public int number() {
    return mqtt5;
  }
}
