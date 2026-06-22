package javasabr.mqtt.model.reason.code;

import javasabr.mqtt.model.NumberedEnumLookup;
import javasabr.rlib.common.util.NumberedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors
@RequiredArgsConstructor
public enum PublishAckReasonCode implements NumberedEnum<PublishAckReasonCode>, ReasonCode {
  /**
   * The message is accepted. Publication of the QoS 1 message proceeds.
   */
  SUCCESS(0x00),
  /**
   * The message is accepted but there are no subscribers. This is sent only by the Server. If the Server knows that
   * there are no matching subscribers, it MAY use this Reason Code instead of 0x00 (Success).
   */
  NO_MATCHING_SUBSCRIBERS(0x10),

  // ERRORS

  /**
   * The receiver does not accept the publish but either does not want to reveal the reason, or it does not match one of
   * the other values.
   */
  UNSPECIFIED_ERROR(0x80),
  /**
   * The PUBLISH is valid but the receiver is not willing to accept it.
   */
  IMPLEMENTATION_SPECIFIC_ERROR(0x83),
  /**
   * The PUBLISH is not authorized.
   */
  NOT_AUTHORIZED(0x87),
  /**
   * The Topic Name is not malformed, but is not accepted by this Client or Server.
   */
  TOPIC_NAME_INVALID(0x90),
  /**
   * The Packet Identifier is already in use. This might indicate a mismatch in the Session State between the Client and
   * Server.
   */
  PACKET_IDENTIFIER_IN_USE(0x91),
  /**
   * An implementation or administrative imposed limit has been exceeded.
   */
  QUOTA_EXCEEDED(0x97),
  /**
   * The payload format does not match the specified Payload Format Indicator.
   */
  PAYLOAD_FORMAT_INVALID(0x99);

  private static final NumberedEnumLookup<PublishAckReasonCode> NUMBERED_MAP =
      new NumberedEnumLookup<>(PublishAckReasonCode.values());

  public static PublishAckReasonCode ofCode(int code) {
    return NUMBERED_MAP.require(code);
  }

  private final int code;

  @Override
  public int number() {
    return code;
  }
}
