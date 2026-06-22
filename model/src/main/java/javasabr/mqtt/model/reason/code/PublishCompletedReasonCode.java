package javasabr.mqtt.model.reason.code;

import javasabr.mqtt.model.NumberedEnumLookup;
import javasabr.rlib.common.util.NumberedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors
@RequiredArgsConstructor
public enum PublishCompletedReasonCode implements NumberedEnum<PublishCompletedReasonCode>, ReasonCode {

  /**
   * Packet Identifier released. Publication of QoS 2 message is complete.
   */
  SUCCESS(0x00),
  /**
   * The Packet Identifier is not known. This is not an error during recovery, but at other times indicates a mismatch
   * between the Session State on the Client and Server.
   */
  PACKET_IDENTIFIER_NOT_FOUND(0x92);

  private static final NumberedEnumLookup<PublishCompletedReasonCode> NUMBERED_MAP =
      new NumberedEnumLookup<>(PublishCompletedReasonCode.values());

  public static PublishCompletedReasonCode ofCode(int code) {
    return NUMBERED_MAP.require(code);
  }

  private final int code;

  @Override
  public int number() {
    return code;
  }
}
