//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.model.acl.Operation

abstract class SubscribeAclRuleBuilder extends AclRuleBuilder {
  SubscribeAclRuleBuilder(Action action) {
    super(action, Operation.SUBSCRIBE)
  }
}
