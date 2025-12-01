package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;

public record UserNameCondition(ValueMatcher<String> userNameMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return userNameMatcher.test(requestedUser.userName());
  }
}
