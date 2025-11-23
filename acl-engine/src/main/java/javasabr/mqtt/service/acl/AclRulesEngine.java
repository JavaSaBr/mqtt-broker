package javasabr.mqtt.service.acl;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javasabr.mqtt.model.acl.Action;
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
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AclRulesEngine {

  Array<@NonNull Rule> rules;

  LockableOperations<LockableRefToRefDictionary<@NonNull String, @NonNull Pattern>> patternCache =
      new StampedLockBasedHashBasedRefToRefDictionary<@NonNull String, @NonNull Pattern>().operations();
  LockableOperations<LockableRefToRefDictionary<@NonNull String, @NonNull Matcher>> matcherCache =
      new StampedLockBasedHashBasedRefToRefDictionary<@NonNull String, @NonNull Matcher>().operations();
  LockableOperations<LockableRefToRefDictionary<@NonNull CallId, @NonNull Boolean>> permissionCache =
      new StampedLockBasedHashBasedRefToRefDictionary<@NonNull CallId, @NonNull Boolean>().operations();
  LockableOperations<LockableRefToRefDictionary<@NonNull String, @NonNull TopicFilter>> topicFilterCache =
      new StampedLockBasedHashBasedRefToRefDictionary<@NonNull String, @NonNull TopicFilter>().operations();

  public boolean authorize(String username, String clientId, String ipAddress, Action action, String topic) {
    CallId callId = new CallId(username, clientId, ipAddress, action, topic);
    return permissionCache.getInWriteLock(callId, (map, call) -> map.getOrCompute(call, this::getPermission));
  }

  private boolean getPermission(CallId callId) {
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
    if (clients == Clients.ALL) {
      return true;
    }
    switch (clients.operator()) {
      case AND -> {
        return checkUsername(clients, callId) && checkClientId(clients, callId) && checkIpAddress(clients, callId)
            && checkAttributes(clients, callId);
      }
      case OR -> {
        return checkUsername(clients, callId) || checkClientId(clients, callId) || checkIpAddress(clients, callId)
            || checkAttributes(clients, callId);
      }
      default -> {
        return false;
      }
    }
  }

  private boolean checkMatches(
      Clients clients,
      CallId c,
      Function<Clients, List<String>> ruleValueGetter,
      Function<CallId, String> requestedValueGetter) {
    for (String ruleValue : ruleValueGetter.apply(clients)) {
      if (isRegex(ruleValue)) {
        Pattern pattern = patternCache.getInWriteLock(ruleValue, AclRulesEngine::getPattern);
        Matcher matcher = matcherCache.getInWriteLock(
            requestedValueGetter.apply(c),
            pattern,
            AclRulesEngine::getMatcher);
        if (matcher.matches()) {
          return true;
        }
      } else {
        if (Objects.equals(requestedValueGetter.apply(c), ruleValue)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean checkUsername(Clients clients, CallId c) {
    return checkMatches(clients, c, Clients::usernames, CallId::username);
  }

  private boolean checkClientId(Clients clients, CallId c) {
    return checkMatches(clients, c, Clients::clientIds, CallId::clientId);
  }

  private boolean checkIpAddress(Clients clients, CallId c) {
    return clients
        .ipAddresses()
        .contains(c.ipAddress());
  }

  private static Pattern getPattern(LockableRefToRefDictionary<@NonNull String, @NonNull Pattern> map, String un) {
    return map.getOrCompute(un, u -> Pattern.compile(trimSlashes(u)));
  }

  private static Matcher getMatcher(
      LockableRefToRefDictionary<@NonNull String, @NonNull Matcher> map,
      String un,
      Pattern pattern) {
    return map.getOrCompute(un, pattern::matcher);
  }

  private boolean checkAttributes(Clients clients, CallId c) {
    return matchesAttributes(clients, c);
  }

  private boolean matchesAttributes(Clients clients, CallId callId) {
    return true;
  }

  private static boolean isRegex(String string) {
    return string.length() > 1 && string.startsWith("/") && string.endsWith("/");
  }

  private static String trimSlashes(String regex) {
    return regex.substring(1, regex.length() - 2);
  }

  private boolean matchesTopic(List<String> ruleTopicFilters, String requestedTopicName) {
    if (ruleTopicFilters == null || ruleTopicFilters.isEmpty()) {
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
