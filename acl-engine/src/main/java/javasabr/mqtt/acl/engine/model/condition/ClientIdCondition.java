package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;

public record ClientIdCondition(ValueMatcher<String> expectedClientId) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return expectedClientId.test(requestedUser.clientId());
  }
}
