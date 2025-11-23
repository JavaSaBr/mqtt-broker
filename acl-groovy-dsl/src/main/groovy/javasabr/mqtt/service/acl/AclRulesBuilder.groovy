//file:noinspection unused
package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Clients
import javasabr.mqtt.model.acl.Operator
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static groovy.lang.Closure.DELEGATE_ONLY

/**
 * Builds list of {@link javasabr.mqtt.model.acl.Rule} from ACL configuration
 */
class AclRulesBuilder {

  private MutableArray<Rule> rules = MutableArray.ofType(Rule.class)

  Array<Rule> build() {
    return Array.copyOf(rules)
  }

  private AclRulesBuilder rule(String name, @DelegatesTo(strategy = DELEGATE_ONLY) Closure config) {
    def block = new RuleBuilder(name)
    config.delegate = block
    config()
    rules << block.build()
    this
  }

  private static class RuleBuilder {
    private String name
    private Permission permission
    private Action action
    private List<String> topics = []
    private Clients clients

    private RuleBuilder(String name) { this.name = name }

    private RuleBuilder permission(Permission permission) { this.permission = permission; this }

    private RuleBuilder action(Action action) { this.action = action; this }

    private RuleBuilder topics(String... topics) { this.topics.addAll(topics); this }

    private RuleBuilder clients(Operator operator, @DelegatesTo(strategy = DELEGATE_ONLY) Closure config) {
      if (this.clients) throw new IllegalArgumentException("Only one clients section allowed")
      ClientsBuilder clientBuilder = new ClientsBuilder(operator)
      config.delegate = clientBuilder
      config()
      this.clients = clientBuilder.build()
      return this
    }

    private RuleBuilder clients(Operator operator) { this.clients = Clients.ALL; this }

    private Rule build() { new Rule(name, permission, action, clients ?: Clients.ALL, topics) }
  }

  private static class ClientsBuilder {
    private Operator operator = Operator.OR
    private List<String> usernames = []
    private List<String> clientIds = []
    private Map<String, String> clientAttrs = [:]
    private List<String> ipAddresses = []

    private ClientsBuilder(Operator operator) { this.operator = operator }

    private ClientsBuilder username(String... usernames) { this.usernames.addAll(usernames); this }

    private ClientsBuilder clientId(String... clientIds) { this.clientIds.addAll(clientIds); this }

    private ClientsBuilder clientAttr(Map<String, String> clientAttrs) { this.clientAttrs.putAll(clientAttrs); this }

    private ClientsBuilder ipaddr(String... ipAddresses) { this.ipAddresses.addAll(ipAddresses); this }

    private Clients build() { new Clients(operator, usernames, clientIds, ipAddresses) }
  }
}
