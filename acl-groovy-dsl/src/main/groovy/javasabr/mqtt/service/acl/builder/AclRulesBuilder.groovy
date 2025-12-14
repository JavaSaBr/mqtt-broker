//file:noinspection unused
package javasabr.mqtt.service.acl.builder

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

  void allowPublish(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowPublishRuleBuilder(), config))
  }

  void denyPublish(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenyPublishRuleBuilder(), config))
  }

  void allowSubscribe(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowSubscribeRuleBuilder(), config))
  }

  void denySubscribe(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenySubscribeRuleBuilder(), config))
  }

  private static CompletableFuture<Rule> startBuilderAsync(RuleBuilder builder, Closure<?> config) {
    CompletableFuture.supplyAsync({ putConfigToBuilder(builder, config).build() })
  }

  private static RuleBuilder putConfigToBuilder(
      RuleBuilder ruleBuilder,
      Closure<?> ruleConfigurator) {
    ruleConfigurator.delegate = ruleBuilder
    ruleConfigurator.resolveStrategy = Closure.DELEGATE_ONLY
    ruleConfigurator()
    return ruleBuilder
  }
}
