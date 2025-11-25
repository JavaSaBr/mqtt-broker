//file:noinspection unused
package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.AllClients
import javasabr.mqtt.model.acl.AnyClient
import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.ClientComparator
import javasabr.mqtt.model.acl.Clients
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY

/**
 * Builds list of {@link javasabr.mqtt.model.acl.Rule} from ACL configuration
 */
class AclRulesBuilder {

  private MutableArray<Rule> rules = MutableArray.ofType(Rule.class)

  Array<Rule> build() {
    return Array.copyOf(rules)
  }

  AclRulesBuilder allowPublish(@DelegatesTo(strategy = DELEGATE_FIRST) Closure<?> config) {
    return rule(Permission.ALLOW, PublishRuleBuilder.&new, config)
  }

  AclRulesBuilder denyPublish(@DelegatesTo(strategy = DELEGATE_FIRST) Closure<?> config) {
    return rule(Permission.DENY, PublishRuleBuilder.&new, config)
  }

  AclRulesBuilder allowSubscribe(@DelegatesTo(strategy = DELEGATE_FIRST) Closure<?> config) {
    return rule(Permission.ALLOW, SubscribeRuleBuilder.&new, config)
  }

  AclRulesBuilder denySubscribe(
      @DelegatesTo(strategy = DELEGATE_FIRST) Closure<?> config) {
    return rule(Permission.DENY, SubscribeRuleBuilder.&new, config)
  }

  private AclRulesBuilder rule(
      Permission permission,
      Closure<RuleBuilder> ruleBuilderConstructor,
      @DelegatesTo(strategy = DELEGATE_FIRST) Closure<?> config) {
    def block = ruleBuilderConstructor(permission)
    config.delegate = block
    config()
    rules << block.build()
    this
  }

  static class PublishRuleBuilder extends RuleBuilder {
    private List<String> topicNames = []

    private PublishRuleBuilder(Permission permission) { super(permission, Action.PUBLISH) }

    private PublishRuleBuilder topicName(String... topicName) { this.topicNames.addAll(topicName); this }

    Rule build() { new Rule(permission, action, clients ?: AllClients.MATCH_ALL, topicNames) }
  }

  static class SubscribeRuleBuilder extends RuleBuilder {
    private List<String> topicFilters = []

    private SubscribeRuleBuilder(Permission permission) { super(permission, Action.SUBSCRIBE) }

    private SubscribeRuleBuilder topicFilter(String... topicFilter) { this.topicFilters.addAll(topicFilter); this }

    Rule build() { new Rule(permission, action, clients ?: AllClients.MATCH_ALL, topicFilters) }
  }

  static abstract class RuleBuilder {
    Permission permission
    Action action
    Clients clients

    RuleBuilder(Permission permission, Action action) { this.permission = permission; this.action = action }

    RuleBuilder allClients(@DelegatesTo(strategy = DELEGATE_ONLY) Closure config) {
      return buildClients(AllClientsBuilder.&new, config)
    }

    RuleBuilder anyClient(@DelegatesTo(strategy = DELEGATE_ONLY) Closure config) {
      return buildClients(AllClientsBuilder.&new, config)
    }

    private RuleBuilder buildClients(
        Closure<ClientsBuilder> clientsBuilderConstructor,
        @DelegatesTo(strategy = DELEGATE_ONLY) Closure config) {
      if (this.clients) throw new IllegalArgumentException("Only one clients section allowed")
      ClientsBuilder clientBuilder = clientsBuilderConstructor()
      config.delegate = clientBuilder
      config()
      this.clients = clientBuilder.build()
      return this
    }

    RuleBuilder allClients() { this.clients = AllClients.MATCH_ALL; this }

    abstract Rule build()
  }

  static abstract class ClientsBuilder implements RegexComparatorBuilder, EqualsComparatorBuilder, ClientMatcherBuilder {
    protected List<ClientComparator> usernames = []
    protected List<ClientComparator> clientIds = []
    protected Map<String, String> clientAttrs = [:]
    protected List<ClientComparator> ipAddresses = []

    ClientsBuilder username(ClientComparator... username) { this.usernames.addAll(username); this }

    ClientsBuilder clientId(ClientComparator... clientId) { this.clientIds.addAll(clientId); this }

    ClientsBuilder clientAttr(Map<String, String> clientAttrs) { this.clientAttrs.putAll(clientAttrs); this }

    ClientsBuilder ipaddr(ClientComparator... ipAddress) { this.ipAddresses.addAll(ipAddress); this }

    abstract Clients build()
  }

  static class AllClientsBuilder extends ClientsBuilder {
    Clients build() {
      new AllClients(
          Array.of(
              match(CallId::username, Array.of(usernames.toArray(ClientComparator[]::new))),
              match(CallId::clientId, Array.of(clientIds.toArray(ClientComparator[]::new))),
              match(CallId::ipAddress, Array.of(ipAddresses.toArray(ClientComparator[]::new)))
          )
      )
    }
  }

  static class AnyClientBuilder extends ClientsBuilder {
    Clients build() {
      new AnyClient(
          Array.of(
              match(CallId::username, Array.of(usernames.toArray(ClientComparator[]::new))),
              match(CallId::clientId, Array.of(clientIds.toArray(ClientComparator[]::new))),
              match(CallId::ipAddress, Array.of(ipAddresses.toArray(ClientComparator[]::new)))
          )
      )
    }
  }
}
