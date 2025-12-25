package javasabr.mqtt.acl.engine;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
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
    Map<Operation, Array<AclRule>> emptyRules = Arrays
        .stream(Operation.values())
        .collect(Collectors.toMap(operation -> operation, _ -> Array.empty(AclRule.class)));
    NO_OPS_ENGINE = new AclEngine(new EnumMap<>(emptyRules));
  }
  
  Map<Operation, Array<AclRule>> ruleMap;
  
  public boolean authorize(MqttUser mqttUser, Operation operation, AbstractTopic topic) {
    Array<AclRule> rules = ruleMap.get(operation);
    for (AclRule rule : rules) {
      if (rule.test(mqttUser, operation, topic)) {
        return rule.action() == Action.ALLOW;
      }
    }
    return false;
  }
}
