package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.rlib.common.util.StringUtils;
import org.jspecify.annotations.Nullable;

public interface MqttUserCondition extends Condition<MqttUser> {

  MqttUserCondition MATCH_ANY = new AnyCondition();

  default @Nullable String getIdentityValue(MqttUser mqttUser) {
    return StringUtils.EMPTY;
  };

  default boolean test(MqttUser value) {
    return test(getIdentityValue(value));
  }

  default boolean test(@Nullable String value) {
    return false;
  }
}
