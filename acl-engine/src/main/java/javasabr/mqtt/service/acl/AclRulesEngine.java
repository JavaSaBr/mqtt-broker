package javasabr.mqtt.service.acl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.rule.Rule;
import javasabr.rlib.collections.array.Array;

public record AclRulesEngine(Map<Operation, Array<Rule>> ruleMap) {

  public boolean authorize(MqttUser mqttUser, Operation operation, String topic) {
    Array<Rule> rules = ruleMap.get(operation);
    for (Rule rule : rules) {
      if (rule.test(mqttUser, operation, topic)) {
        return rule.action() == Action.ALLOW;
      }
    }
    return false;
  }
}
