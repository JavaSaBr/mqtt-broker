package javasabr.mqtt.acl.engine.builder;

import java.util.EnumMap;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleContainerBuilder {

  private static final Array<AclRule> EMPTY_RULES = Array.empty(AclRule.class);

  public static Map<Operation, Array<AclRule>> groupRulesByOperation(Array<AclRule> rules) {
    var intermediate = new EnumMap<Operation, MutableArray<AclRule>>(Operation.class);
    for (AclRule rule : rules) {
      intermediate
          .computeIfAbsent(rule.operation(), RuleContainerBuilder::newMutableArray)
          .add(rule);
    }
    var finalMap = new EnumMap<Operation, Array<AclRule>>(Operation.class);
    for (var entry : intermediate.entrySet()) {
      finalMap.put(entry.getKey(), Array.copyOf(entry.getValue()));
    }
    Operation.forEach(operation -> {
      finalMap.computeIfAbsent(operation, RuleContainerBuilder::emptyArray);
    });
    // no need to wrap this map because it's not public API
    return finalMap;
  }

  static Array<AclRule> emptyArray(Operation ignored) {
    return EMPTY_RULES;
  }

  static MutableArray<AclRule> newMutableArray(Operation ignored) {
    return ArrayFactory.mutableArray(AclRule.class);
  }
}
