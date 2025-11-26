//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Permission.ALLOW
import static javasabr.mqtt.model.acl.Permission.DENY

/**
 * Builds list of {@link javasabr.mqtt.model.acl.Rule} from ACL configuration
 */
class AclRulesBuilder {

  private MutableArray<Rule> rules = MutableArray.ofType(Rule.class)

  Array<Rule> build() {
    return Array.copyOf(rules)
  }

  AclRulesBuilder allowPublish(Closure<?> config) {
    return rule(ALLOW, PublishRuleBuilder.&new, config)
  }

  AclRulesBuilder denyPublish(Closure<?> config) {
    return rule(DENY, PublishRuleBuilder.&new, config)
  }

  AclRulesBuilder allowSubscribe(Closure<?> config) {
    return rule(ALLOW, SubscribeRuleBuilder.&new, config)
  }

  AclRulesBuilder denySubscribe(Closure<SubscribeRuleBuilder> config) {
    return rule(DENY, SubscribeRuleBuilder.&new, config)
  }

  private AclRulesBuilder rule(
      Permission permission,
      Closure<RuleBuilder> ruleBuilderConstructor,
      Closure<?> ruleConfigurator) {
    def ruleBuilder = ruleBuilderConstructor(permission)
    ruleConfigurator.delegate = ruleBuilder
    ruleConfigurator()
    rules << ruleBuilder.build()
    this
  }
}
