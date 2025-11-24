package javasabr.mqtt.model.reason.code;

import javasabr.rlib.common.util.NumberedEnum;
import javasabr.rlib.common.util.NumberedEnumMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors
@RequiredArgsConstructor
public enum SubscribeAckReasonCode implements NumberedEnum<SubscribeAckReasonCode>, ReasonCode {
  /**
   * The subscription is accepted and the maximum QoS sent will be QoS 0. This might be a lower QoS than was requested.
   */
  GRANTED_QOS_0(0x00),
  /**
   * The subscription is accepted and the maximum QoS sent will be QoS 1. This might be a lower QoS than was requested.
   */
  GRANTED_QOS_1(0x01),
  /**
   * The subscription is accepted and any received QoS will be sent to this subscription.
   */
  GRANTED_QOS_2(0x02),

  // ERRORS

  /**
   * The subscription is not accepted and the Server either does not wish to reveal the reason or none of the other
   * Reason Codes apply.
   */
  UNSPECIFIED_ERROR(0x80),
  /**
   * The SUBSCRIBE is valid but the Server does not accept it.
   */
  IMPLEMENTATION_SPECIFIC_ERROR(0x83),
  /**
   * The Client is not authorized to make this subscription.
   */
  NOT_AUTHORIZED(0x87),
  /**
   * The Topic Filter is correctly formed but is not allowed for this Client.
   */
  TOPIC_FILTER_INVALID(0x8F),
  /**
   * The specified Packet Identifier is already in use.
   */
  PACKET_IDENTIFIER_IN_USE(0x91),
  /**
   * An implementation or administrative imposed limit has been exceeded.
   */
  QUOTA_EXCEEDED(0x97),
  /**
   * The Server does not support Shared Subscriptions for this Client.
   */
  SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(0x9E),
  /**
   * The Server does not support Subscription Identifiers; the subscription is not accepted.
   */
  SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(0xA1),
  /**
   * The Server does not support Wildcard Subscriptions; the subscription is not accepted.
   */
  WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(0xA2);

  private static final NumberedEnumMap<SubscribeAckReasonCode> NUMBERED_MAP =
      new NumberedEnumMap<>(SubscribeAckReasonCode.class);

  public static SubscribeAckReasonCode ofCode(int code) {
    return NUMBERED_MAP.require(code);
  }

  private final int code;

  @Override
  public int number() {
    return code;
  }
}
