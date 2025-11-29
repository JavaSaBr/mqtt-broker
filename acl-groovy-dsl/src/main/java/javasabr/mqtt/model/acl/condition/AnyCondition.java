package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.rlib.common.util.StringUtils;

public record AnyCondition() implements MqttUserCondition {

  @Override
  public String getIdentityValue(MqttUser mqttUser) {
    return StringUtils.EMPTY;
  }

  @Override
  public boolean test(MqttUser value) {
    return true;
  }

  @Override
  public boolean test(String value) {
    return true;
  }
}
