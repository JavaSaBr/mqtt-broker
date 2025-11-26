package javasabr.mqtt.model.acl

import groovy.transform.ImmutableOptions
import javasabr.mqtt.model.acl.condition.AnyCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.value.matcher.TopicMatcher
import javasabr.rlib.collections.array.Array

@ImmutableOptions(knownImmutableClasses = Condition)
record Rule(Action action, Operation operation, Condition condition, Array<TopicMatcher<String>> topics) {
  Rule(Action action, Operation operation) {
    this(action, operation, new AnyCondition(), Array.empty(TopicMatcher.class));
  }
}
