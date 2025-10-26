package javasabr.mqtt.model;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum QoS {
  AT_MOST_ONCE(0, SubscribeAckReasonCode.GRANTED_QOS_0),
  AT_LEAST_ONCE(1, SubscribeAckReasonCode.GRANTED_QOS_1),
  EXACTLY_ONCE(2, SubscribeAckReasonCode.GRANTED_QOS_2),
  INVALID(3, SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);

  private static final QoS[] VALUES = values();

  public static QoS of(int level) {
    if (level < 0 || level > EXACTLY_ONCE.ordinal()) {
      return INVALID;
    } else {
      return VALUES[level];
    }
  }

  int index;
  SubscribeAckReasonCode subscribeAckReasonCode;
}
