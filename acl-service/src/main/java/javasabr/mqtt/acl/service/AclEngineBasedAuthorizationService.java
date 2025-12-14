package javasabr.mqtt.acl.service;

import javasabr.mqtt.acl.engine.AclEngine;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.AuthorizationService;

public abstract class AclEngineBasedAuthorizationService implements AuthorizationService {
  
  protected AclEngine engine;

  protected AclEngineBasedAuthorizationService() {
    this.engine = AclEngine.NO_OPS_ENGINE;
  }

  @Override
  public boolean authorizePublish(MqttUser user, TopicName topicName) {
    return engine.authorize(user, Operation.PUBLISH, topicName);
  }

  @Override
  public boolean authorizeSubscribe(MqttUser user, TopicFilter topicFilter) {
    return engine.authorize(user, Operation.SUBSCRIBE, topicFilter);
  }

  // we know that writing this to not volatile field will not apply it for all threads immediately,
  // but for us it's not critical comparing to cost of reading volatile field
  protected synchronized void switchTo(AclEngine newEngine) {
    this.engine = newEngine;
  }
}
