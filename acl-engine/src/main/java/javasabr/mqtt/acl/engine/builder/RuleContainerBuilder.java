package javasabr.mqtt.acl.engine.builder;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javasabr.mqtt.acl.engine.model.rule.Rule;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleContainerBuilder {

  private static final Array<Rule> EMPTY_RULES = Array.empty(Rule.class);

  public static Map<Operation, Array<Rule>> groupRulesByOperation(Array<Rule> rules) {

    var intermediate = new EnumMap<Operation, MutableArray<Rule>>(Operation.class);
    for (Rule rule : rules) {
      intermediate
          .computeIfAbsent(rule.operation(), RuleContainerBuilder::newMutableArray)
          .add(rule);
    }
    var finalMap = new EnumMap<Operation, Array<Rule>>(Operation.class);
    for (var entry : intermediate.entrySet()) {
      finalMap.put(entry.getKey(), Array.copyOf(entry.getValue()));
    }
    Operation.forEach(operation -> {
      finalMap.computeIfAbsent(operation, RuleContainerBuilder::emptyArray);
    });
    return Collections.unmodifiableMap(finalMap);
  }

  static Array<Rule> emptyArray(Operation ignored) {
    return EMPTY_RULES;
  }

  static MutableArray<Rule> newMutableArray(Operation ignored) {
    return ArrayFactory.mutableArray(Rule.class);
  }
}
