//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import groovy.transform.TypeChecked
import javasabr.mqtt.acl.engine.model.rule.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.ArrayFactory

import java.util.concurrent.CompletableFuture

/**
 * Builds list of {@link Rule} from ACL configuration
 */
class AclRulesBuilder {

  private final List<CompletableFuture<Rule>> ruleBuilderFutures = []

  Array<Rule> build() {
    return ruleBuilderFutures.collect(ArrayFactory.mutableArray(Rule.class), { it.join() })
  }
  
  @TypeChecked
  AclRulesBuilder allowPublish(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowPublishRuleBuilder(), config))
    return this
  }

  @TypeChecked
  AclRulesBuilder denyPublish(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenyPublishRuleBuilder(), config))
    return this
  }

  @TypeChecked
  AclRulesBuilder allowSubscribe(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowSubscribeRuleBuilder(), config))
    return this
  }

  @TypeChecked
  AclRulesBuilder denySubscribe(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenySubscribeRuleBuilder(), config))
    return this
  }

  private static CompletableFuture<Rule> startBuilderAsync(RuleBuilder builder, Closure<?> config) {
    CompletableFuture.supplyAsync({ putConfigToBuilder(builder, config).build() })
  }

  private static RuleBuilder putConfigToBuilder(
      RuleBuilder ruleBuilder,
      Closure<?> config) {
    config.delegate = ruleBuilder
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return ruleBuilder
  }
}
