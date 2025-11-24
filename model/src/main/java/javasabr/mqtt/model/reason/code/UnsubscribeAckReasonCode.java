package javasabr.mqtt.model.reason.code;

import javasabr.rlib.common.util.NumberedEnum;
import javasabr.rlib.common.util.NumberedEnumMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors
@RequiredArgsConstructor
public enum UnsubscribeAckReasonCode implements NumberedEnum<UnsubscribeAckReasonCode>, ReasonCode {
  /**
   * The subscription is deleted.
   */
  SUCCESS(0x00),
  /**
   * The subscription is accepted and the maximum QoS sent will be QoS 1. This might be a lower QoS than was requested.
   */
  NO_SUBSCRIPTION_EXISTED(0x11),

  // ERRORS
  /**
   * The unsubscribe could not be completed and the Server either does not wish to reveal the reason or none of the
   * other Reason Codes apply.
   */
  UNSPECIFIED_ERROR(0x80),
  /**
   * The UNSUBSCRIBE is valid but the Server does not accept it.
   */
  IMPLEMENTATION_SPECIFIC_ERROR(0x83),
  /**
   * The Client is not authorized to unsubscribe.
   */
  NOT_AUTHORIZED(0x87),
  /**
   * The Topic Filter is correctly formed but is not allowed for this Client.
   */
  TOPIC_FILTER_INVALID(0x8F),
  /**
   * The specified Packet Identifier is already in use.
   */
  PACKET_IDENTIFIER_IN_USE(0x91);

  private static final NumberedEnumMap<UnsubscribeAckReasonCode> NUMBERED_MAP =
      new NumberedEnumMap<>(UnsubscribeAckReasonCode.class);

  public static UnsubscribeAckReasonCode ofCode(int code) {
    return NUMBERED_MAP.require(code);
  }

  private final int code;

  @Override
  public int number() {
    return code;
  }
}
