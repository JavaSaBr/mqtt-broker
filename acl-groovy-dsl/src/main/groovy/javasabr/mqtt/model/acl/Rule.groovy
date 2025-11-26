package javasabr.mqtt.model.acl

import groovy.transform.ImmutableOptions
import javasabr.mqtt.model.acl.condition.Any
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.value.matcher.TopicMatcher
import javasabr.rlib.collections.array.Array

@ImmutableOptions(knownImmutableClasses = [Condition])
record Rule(
    Permission permission, Action action, Condition condition, Array<TopicMatcher<String>> topics) {
  Rule(Permission permission, Action action) {
    this(permission, action, new Any(), Array.empty(TopicMatcher.class));
  }
}
