package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;

public record ClientIdCondition(ValueMatcher<String> expectedClientId) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return expectedClientId.test(requestedUser.clientId());
  }
}
