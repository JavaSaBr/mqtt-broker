package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import org.jspecify.annotations.Nullable;

public record UserNameCondition(ValueMatcher<String> clientMatcher) implements MqttUserCondition {
  @Override
  public @Nullable String getIdentityValue(MqttUser mqttUser) {
    return mqttUser.userName();
  }

  @Override
  public boolean test(String value) {
    return clientMatcher.test(value);
  }
}
