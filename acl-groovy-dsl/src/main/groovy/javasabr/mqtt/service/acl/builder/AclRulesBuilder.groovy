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
import static javasabr.mqtt.model.acl.Action.ALLOW
import static javasabr.mqtt.model.acl.Action.DENY

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

  void createTask(RuleBuilder builder, Closure<?> config) {
    def ruleFuture = CompletableFuture.supplyAsync {
      return putConfigToBuilder(builder, config).build()
    }
    if (ruleParseTask == null) {
      ruleParseTask = ruleFuture.thenAccept { rule ->
        lockableRules.inWriteLock(rule, (a, r) -> { a.add(r) })
      }
    } else {
      ruleParseTask = ruleParseTask.thenCombine(ruleFuture, { _, r -> r })
          .thenAccept { rule ->
            lockableRules.inWriteLock(rule, (a, r) -> { a.add(r) })
          }
    }
  }

  void allowPublish(Closure<?> config) {
    createTask(new PublishRuleBuilder(ALLOW), config)
  }

  void denyPublish(Closure<?> config) {
    createTask(new PublishRuleBuilder(DENY), config)
  }

  void allowSubscribe(Closure<?> config) {
    createTask(new SubscribeRuleBuilder(ALLOW), config)
  }

  void denySubscribe(Closure<SubscribeRuleBuilder> config) {
    createTask(new SubscribeRuleBuilder(DENY), config)
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
