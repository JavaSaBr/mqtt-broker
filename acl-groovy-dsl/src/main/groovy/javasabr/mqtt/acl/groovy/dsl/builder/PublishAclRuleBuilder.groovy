//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.model.acl.Operation

abstract class PublishAclRuleBuilder extends AclRuleBuilder {
  PublishAclRuleBuilder(Action action) {
    super(action, Operation.PUBLISH)
  }
}
