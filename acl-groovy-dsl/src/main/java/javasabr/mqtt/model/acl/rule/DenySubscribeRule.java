package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;
import static javasabr.mqtt.model.acl.condition.TopicCondition.MATCH_ANY;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;

public record DenySubscribeRule(MqttUserCondition clientCondition, TopicCondition topicCondition) implements Rule {

  public DenySubscribeRule(MqttUserCondition clients) {
    this(clients, MATCH_ANY);
  }

  public DenySubscribeRule(TopicCondition topics) {
    this(MqttUserCondition.MATCH_ANY, topics);
  }

  @Override
  public Operation operation() {
    return SUBSCRIBE;
  }

  @Override
  public Action action() {
    return DENY;
  }
}
