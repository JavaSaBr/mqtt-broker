package javasabr.mqtt.acl.engine;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.acl.engine.model.rule.Rule;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AclEngine {
  
  public static final AclEngine NO_OPS_ENGINE;

  static {
    Map<Operation, Array<Rule>> emptyRules = Arrays
        .stream(Operation.values())
        .collect(Collectors.toMap(operation -> operation, _ -> Array.empty(Rule.class)));
    NO_OPS_ENGINE = new AclEngine(new EnumMap<>(emptyRules));
  }
  
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
