package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record UserNameCondition(ValueMatcher<String> userNameMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return userNameMatcher.test(requestedUser.userName());
  }
}
