package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;

public record UserNameCondition(ValueMatcher<String> userNameMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return userNameMatcher.test(requestedUser.userName());
  }
}
