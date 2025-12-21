package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record IpAddressCondition(ValueMatcher<String> ipAddressMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser user) {
    return ipAddressMatcher.test(user.ipAddress());
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("ipAddress", ipAddressMatcher);
  }
}
