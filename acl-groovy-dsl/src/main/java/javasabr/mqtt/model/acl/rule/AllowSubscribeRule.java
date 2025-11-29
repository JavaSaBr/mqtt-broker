package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.ALLOW;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;
import static javasabr.mqtt.model.acl.condition.TopicCondition.MATCH_ANY;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;

public record AllowSubscribeRule(MqttUserCondition clientCondition, TopicCondition topicCondition) implements Rule {

  public AllowSubscribeRule(MqttUserCondition clients) {
    this(clients, MATCH_ANY);
  }

  public AllowSubscribeRule(TopicCondition topics) {
    this(MqttUserCondition.MATCH_ANY, topics);
  }

  public AllowSubscribeRule() {
    this(MqttUserCondition.MATCH_ANY, TopicCondition.MATCH_ANY);
  }

  @Override
  public Operation operation() {
    return SUBSCRIBE;
  }

  @Override
  public Action action() {
    return ALLOW;
  }
}
