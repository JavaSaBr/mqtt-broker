package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;

public record IpAddressCondition(ValueMatcher<String> clientMatcher) implements MqttUserCondition {
  @Override
  public String getIdentityValue(MqttUser mqttUser) {
    return mqttUser.ipAddress();
  }

  @Override
  public boolean test(String value) {
    return clientMatcher.test(value);
  }
}
