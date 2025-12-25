package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record IpAddressCondition(ValueMatcher<String> matcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser user) {
    return matcher.test(user.ipAddress());
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("ipAddress", matcher);
  }
}
