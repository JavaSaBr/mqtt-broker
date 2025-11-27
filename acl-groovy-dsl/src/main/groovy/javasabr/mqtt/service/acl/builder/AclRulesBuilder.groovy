//file:noinspection unused
package javasabr.mqtt.service.acl.builder


import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Action.ALLOW
import static javasabr.mqtt.model.acl.Action.DENY

/**
 * Builds list of {@link javasabr.mqtt.model.acl.rule.Rule} from ACL configuration
 */
class AclRulesBuilder {

  private MutableArray<Rule> rules = MutableArray.ofType(Rule.class)

  Array<Rule> build() {
    return Array.copyOf(rules)
  }

  AclRulesBuilder allowPublish(Closure<?> config) {
    def builder = new PublishRuleBuilder(ALLOW)
    return rule(builder, config)
  }

  AclRulesBuilder denyPublish(Closure<?> config) {
    def builder = new PublishRuleBuilder(DENY)
    return rule(builder, config)
  }

  AclRulesBuilder allowSubscribe(Closure<?> config) {
    def builder = new SubscribeRuleBuilder(ALLOW)
    return rule(builder, config)
  }

  AclRulesBuilder denySubscribe(Closure<SubscribeRuleBuilder> config) {
    def builder = new SubscribeRuleBuilder(DENY)
    return rule(builder, config)
  }

  private AclRulesBuilder rule(
      RuleBuilder ruleBuilder,
      Closure<?> ruleConfigurator) {
    ruleConfigurator.delegate = ruleBuilder
    ruleConfigurator()
    rules << ruleBuilder.build()
    this
  }
}
