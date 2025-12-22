package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record UserNameCondition(ValueMatcher<String> matcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return matcher.test(requestedUser.userName());
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("userName", matcher);
  }
}
