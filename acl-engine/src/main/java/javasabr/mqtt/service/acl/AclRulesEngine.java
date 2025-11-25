package javasabr.mqtt.service.acl;

import java.util.List;
import javasabr.mqtt.model.acl.AllClients;
import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.Clients;
import javasabr.mqtt.model.acl.Permission;
import javasabr.mqtt.model.acl.Rule;
import javasabr.mqtt.model.topic.TopicFilter;
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
  LockableOperations<LockableRefToRefDictionary<String, TopicFilter>> topicFilterCache =
      new StampedLockBasedHashBasedRefToRefDictionary<String, TopicFilter>().operations();

  public boolean authorize(CallId callId) {
    return permissionCache.getInWriteLock(callId, this, (map, call, eng) -> map.getOrCompute(call, eng::isAllowed));
  }

  private boolean isAllowed(CallId callId) {
    for (Rule rule : rules) {
      if (rule.action() != callId.action()) {
        continue;
      }
      if (!matchesTopic(rule.topics(), callId.topic())) {
        continue;
      }
      if (!matchesClient(rule.clients(), callId)) {
        continue;
      }
      return rule.permission() == Permission.ALLOW;
    }
    return false;
  }

  private boolean matchesClient(Clients clients, CallId callId) {
    return clients == AllClients.MATCH_ALL || clients.match(callId);
  }

  private boolean checkAttributes(Clients clients, CallId c) {
    return matchesAttributes(clients, c);
  }

  private boolean matchesAttributes(Clients clients, CallId callId) {
    return true;
  }

  private boolean matchesTopic(List<String> ruleTopicFilters, String requestedTopicName) {
    if (ruleTopicFilters.isEmpty()) {
      return false;
    }
    for (String ruleTopicFilter : ruleTopicFilters) {
      TopicFilter topicFilter = topicFilterCache.getInWriteLock(
          ruleTopicFilter,
          (map, filter) -> map.getOrCompute(filter, TopicFilter::valueOf));

      if (topicFilter.matches(requestedTopicName)) {
        return true;
      }
    }
    return false;
  }
}
