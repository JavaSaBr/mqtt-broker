package javasabr.mqtt.service.acl;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.rule.Rule;
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
      if (rule.test(callId)) {
        return rule.action() == Action.ALLOW;
      }
    }
    return false;
  }
}
