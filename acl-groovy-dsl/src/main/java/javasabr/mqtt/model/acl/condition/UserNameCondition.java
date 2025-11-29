package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;

public record UserNameCondition(ValueMatcher<String> clientMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser value) {
    return clientMatcher.test(value.userName());
  }
}
