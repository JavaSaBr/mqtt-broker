package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.ALLOW;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;
import static javasabr.mqtt.model.acl.condition.TopicCondition.MATCH_ANY;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;

public record AllowPublishRule(MqttUserCondition clientCondition, TopicCondition topicCondition) implements Rule {

  public AllowPublishRule(MqttUserCondition clients) {
    this(clients, MATCH_ANY);
  }

  public AllowPublishRule(TopicCondition topics) {
    this(MqttUserCondition.MATCH_ANY, topics);
  }

  @Override
  public Operation operation() {
    return PUBLISH;
  }

  @Override
  public Action action() {
    return ALLOW;
  }
}
