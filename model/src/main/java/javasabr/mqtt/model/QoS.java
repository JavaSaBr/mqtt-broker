package javasabr.mqtt.model;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.rlib.common.util.NumberedEnum;
import javasabr.rlib.common.util.NumberedEnumMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum QoS implements NumberedEnum<QoS> {
  AT_MOST_ONCE(0, SubscribeAckReasonCode.GRANTED_QOS_0),
  AT_LEAST_ONCE(1, SubscribeAckReasonCode.GRANTED_QOS_1),
  EXACTLY_ONCE(2, SubscribeAckReasonCode.GRANTED_QOS_2),
  INVALID(3, SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);

  private static final NumberedEnumMap<QoS> NUMBERED_MAP =
      new NumberedEnumMap<>(QoS.class);

  public static QoS ofCode(int level) {
    return NUMBERED_MAP.resolve(level, QoS.INVALID);
  }

  int level;
  SubscribeAckReasonCode subscribeAckReasonCode;

  @Override
  public int number() {
    return level;
  }

  public QoS lower(QoS alternative) {
    return level > alternative.level ? alternative : this;
  }

  public boolean isLowerThan(QoS another) {
    return level < another.level;
  }

  public boolean isHigherThan(QoS another) {
    return level > another.level;
  }
}
