//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.LockableArray
import javasabr.rlib.collections.operation.LockableOperations

import java.util.concurrent.CompletableFuture

import static java.lang.System.currentTimeMillis
import static java.lang.System.out

/**
 * Builds list of {@link javasabr.mqtt.model.acl.rule.Rule} from ACL configuration
 */
class AclRulesBuilder {

  private LockableArray<Rule> rules = ArrayFactory.stampedLockBasedArray(Rule)
  private LockableOperations<LockableArray<Rule>> lockableRules = rules.operations()
  private CompletableFuture<Void> ruleParseTask = null
  private long creationTime = currentTimeMillis()

  Array<Rule> build() {
    ruleParseTask.join()
    out.println("ACL config is parsed in %s ms".formatted(currentTimeMillis() - creationTime))
    return Array.copyOf(rules)
  }

  void startBuilderAsync(RuleBuilder builder, Closure<?> config) {
    def applyConfigFuture = CompletableFuture.supplyAsync {
      return putConfigToBuilder(builder, config).build()
    }
    if (ruleParseTask == null) {
      ruleParseTask = applyConfigFuture.thenAccept { rule ->
        lockableRules.inWriteLock(rule, (a, r) -> { a.add(r) })
      }
    } else {
      ruleParseTask = ruleParseTask.thenCombine(applyConfigFuture, { _, r -> r })
          .thenAccept { rule ->
            lockableRules.inWriteLock(rule, (a, r) -> { a.add(r) })
          }
    }
  }

  void allowPublish(Closure<?> config) {
    startBuilderAsync(new AllowPublishRuleBuilder(), config)
  }

  void denyPublish(Closure<?> config) {
    startBuilderAsync(new DenyPublishRuleBuilder(), config)
  }

  void allowSubscribe(Closure<?> config) {
    startBuilderAsync(new AllowSubscribeRuleBuilder(), config)
  }

  void denySubscribe(Closure<SubscribeRuleBuilder> config) {
    startBuilderAsync(new DenySubscribeRuleBuilder(), config)
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
