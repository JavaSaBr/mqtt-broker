package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record UserNameCondition(ValueMatcher<String> userNameMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser user) {
    String userName = user.userName();
    if (userName == null) {
      return userNameMatcher == ValueMatcher.MATCH_ANY_STRING;
    }
    return userNameMatcher.test(userName);
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("userName", userNameMatcher);
  }
}
