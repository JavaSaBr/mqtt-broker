package javasabr.mqtt.acl.engine;

import java.util.Map;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.acl.engine.model.rule.Rule;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AclEngine {
  
  Map<Operation, Array<Rule>> ruleMap;
  
  public boolean authorize(MqttUser mqttUser, Operation operation, AbstractTopic topic) {
    Array<Rule> rules = ruleMap.get(operation);
    for (Rule rule : rules) {
      if (rule.test(mqttUser, operation, topic)) {
        return rule.action() == Action.ALLOW;
      }
    }
    return false;
  }
}
