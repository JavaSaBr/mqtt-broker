//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.ArrayFactory

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

import static java.lang.Runtime.getRuntime
import static java.util.concurrent.CompletableFuture.supplyAsync
import static java.util.concurrent.Executors.newFixedThreadPool
import static javasabr.rlib.collections.array.ArrayFactory.mutableArray

/**
 * Builds list of {@link javasabr.mqtt.model.acl.rule.Rule} from ACL configuration
 */
class AclRulesBuilder {

  private final List<CompletableFuture<Rule>> ruleBuilderFutures = []
  private final Executor executor = newFixedThreadPool(getRuntime().availableProcessors() * 2)

  Array<Rule> build() {
    return ruleBuilderFutures.collect(mutableArray(Rule.class), CompletableFuture::join as Closure<? extends Rule>)
  }

  void allowPublish(Closure<?> config) {
    ruleBuilderFutures << startBuilderAsync(new AllowPublishRuleBuilder(), config)
  }

  void denyPublish(Closure<?> config) {
    ruleBuilderFutures << startBuilderAsync(new DenyPublishRuleBuilder(), config)
  }

  void allowSubscribe(Closure<?> config) {
    ruleBuilderFutures << startBuilderAsync(new AllowSubscribeRuleBuilder(), config)
  }

  void denySubscribe(Closure<?> config) {
    ruleBuilderFutures << startBuilderAsync(new DenySubscribeRuleBuilder(), config)
  }

  CompletableFuture<Rule> startBuilderAsync(RuleBuilder builder, Closure<?> config) {
    supplyAsync({ putConfigToBuilder(builder, config).build() }, executor)
  }

  private static RuleBuilder putConfigToBuilder(
      RuleBuilder ruleBuilder,
      Closure<?> ruleConfigurator) {
    ruleConfigurator.delegate = ruleBuilder
    ruleConfigurator.resolveStrategy = Closure.DELEGATE_ONLY
    ruleConfigurator()
    ruleBuilder
  }
}
