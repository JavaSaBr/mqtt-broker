package javasabr.mqtt.service.acl;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.Rule;
import javasabr.mqtt.model.acl.condition.Condition;
import javasabr.mqtt.model.acl.matcher.TopicMatcher;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.impl.StampedLockBasedHashBasedRefToRefDictionary;
import javasabr.rlib.collections.operation.LockableOperations;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AclRulesEngine {

  Array<Rule> rules;

  LockableOperations<LockableRefToRefDictionary<CallId, Boolean>> permissionCache =
      new StampedLockBasedHashBasedRefToRefDictionary<CallId, Boolean>().operations();

  public boolean authorize(CallId callId) {
    return permissionCache.getInWriteLock(callId, this, (map, call, eng) -> map.getOrCompute(call, eng::isAllowed));
  }

  private boolean isAllowed(CallId callId) {
    for (Rule rule : rules) {
      if (rule.operation() != callId.operation()) {
        continue;
      }
      if (!matchesTopic(rule.topics(), callId.topic())) {
        continue;
      }
      if (!matchesClient(rule.condition(), callId)) {
        continue;
      }
      return rule.action() == Action.ALLOW;
    }
    return false;
  }

  private boolean matchesClient(Condition clients, CallId callId) {
    return clients.test(callId);
  }

  private boolean matchesTopic(Array<TopicMatcher<String>> ruleTopicFilters, String requestedTopicName) {
    if (ruleTopicFilters.isEmpty()) {
      return false;
    }
    for (TopicMatcher<String> ruleTopicFilter : ruleTopicFilters) {
      if (ruleTopicFilter.test(requestedTopicName)) {
        return true;
      }
    }
    return false;
  }
}
