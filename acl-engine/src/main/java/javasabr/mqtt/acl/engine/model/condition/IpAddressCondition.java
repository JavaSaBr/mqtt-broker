package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record IpAddressCondition(ValueMatcher<String> expectedIpAddress) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return expectedIpAddress.test(requestedUser.ipAddress());
  }
}
