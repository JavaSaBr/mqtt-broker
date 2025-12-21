package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.model.MqttUser;

public record ClientIdCondition(ValueMatcher<String> clientIdMatcher) implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return clientIdMatcher.test(requestedUser.clientId());
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("clientId", clientIdMatcher);
  }
}
