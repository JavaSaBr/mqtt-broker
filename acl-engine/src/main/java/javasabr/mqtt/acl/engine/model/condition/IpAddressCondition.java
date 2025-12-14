package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;

public record IpAddressCondition(ValueMatcher<String> expectedIpAddress) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return expectedIpAddress.test(requestedUser.ipAddress());
  }
}
