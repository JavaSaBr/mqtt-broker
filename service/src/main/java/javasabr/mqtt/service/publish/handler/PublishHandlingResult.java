package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum PublishHandlingResult {
  SUCCESS(false, PublishAckReasonCode.SUCCESS, PublishReceivedReasonCode.SUCCESS),
  SKIPPED(false, PublishAckReasonCode.SUCCESS, PublishReceivedReasonCode.SUCCESS),

  // ERRORS
  UNSPECIFIED_ERROR(true, PublishAckReasonCode.UNSPECIFIED_ERROR, PublishReceivedReasonCode.UNSPECIFIED_ERROR),
  IMPLEMENTATION_SPECIFIC_ERROR(
      true,
      PublishAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR,
      PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
  NOT_AUTHORIZED(true, PublishAckReasonCode.NOT_AUTHORIZED, PublishReceivedReasonCode.NOT_AUTHORIZED),
  TOPIC_NAME_INVALID(true, PublishAckReasonCode.TOPIC_NAME_INVALID, PublishReceivedReasonCode.TOPIC_NAME_INVALID),
  PACKET_IDENTIFIER_IN_USE(
      true,
      PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE,
      PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE),
  QUOTA_EXCEEDED(true, PublishAckReasonCode.QUOTA_EXCEEDED, PublishReceivedReasonCode.QUOTA_EXCEEDED),
  PAYLOAD_FORMAT_INVALID(
      true,
      PublishAckReasonCode.PAYLOAD_FORMAT_INVALID,
      PublishReceivedReasonCode.PAYLOAD_FORMAT_INVALID),

  // CUSTOM
  NOT_EXPECTED_CLIENT(
      true,
      PublishAckReasonCode.UNSPECIFIED_ERROR,
      PublishReceivedReasonCode.UNSPECIFIED_ERROR);

  boolean error;
  PublishAckReasonCode ackReasonCode;
  PublishReceivedReasonCode receivedReasonCode;
}
