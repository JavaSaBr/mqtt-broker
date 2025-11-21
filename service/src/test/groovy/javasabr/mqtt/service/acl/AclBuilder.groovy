//file:noinspection unused
package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operator
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.Clients

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Builds list of {@link javasabr.mqtt.model.acl.Rule} from ACL configuration
 */
class AclBuilder {

  private Map root = [
      acl  : [:],
      user : [],
      group: [],
      rule : []
  ]

  List<Rule> result() {
    mapRules(root.rule)
  }

  AclBuilder rule(
      String name,
      @DelegatesTo(strategy = DELEGATE_FIRST, value = RuleBlock) Closure c) {
    def block = new RuleBlock(name)
    c.delegate = block
    c.resolveStrategy = DELEGATE_FIRST
    c()
    root.rule << block.toMap()
    this
  }

  static class RuleBlock {
    String name
    Permission permission
    Action action
    List<String> topics = []
    ClientsBlock clients = new ClientsBlock()

    RuleBlock(String n) { name = n }

    void permission(Permission p) { permission = p }

    void action(Action a) { action = a }

    void topics(String... t) { topics.addAll(t) }

    void clients(@DelegatesTo(strategy = DELEGATE_FIRST, value = ClientsBlock) Closure c) {
      c.delegate = clients
      c.resolveStrategy = DELEGATE_FIRST
      c()
    }

    Map toMap() { [
        name: name,
        permission: permission,
        action: action,
        topics: topics,
        clients: clients.toMap()
    ] }
  }

  static class ClientsBlock {
    Operator operator = Operator.OR
    List<String> usernames = []
    List<String> clientIds = []
    List<String> clientAttrs = []
    List<String> ipAddresses = []

    void operator(Operator op) { operator = op }

    void username(String... u) { usernames.addAll(u) }

    void clientId(String... u) { clientIds.addAll(u) }

    void clientAttr(String... u) { clientAttrs.addAll(u) }

    void ipaddr(String... ips) { ipAddresses.addAll(ips) }

    Map toMap() { [
        operator: operator,
        usernames: usernames,
        clientIds: clientIds,
        clientAttrs: clientAttrs,
        ipAddresses: ipAddresses
    ] }
  }

  private static List<Rule> mapRules(List<Map> raw) {
    raw.collect { m ->
      new Rule(
          m.name as String,
          m.permission as Permission,
          m.action as Action,
          mapClients(m.clients as Map),
          toStrList(m.topics)
      )
    }
  }

  private static Clients mapClients(Map m) {
    if (!m) return new Clients(Operator.OR, List.of(), List.of(), List.of(), List.of())
    return new Clients(
        m.operator as Operator,
        toStrList(m.usernames),
        toStrList(m.clientIds),
        toStrList(m.clientAttrs),
        toStrList(m.ipAddresses))
  }

  private static List<String> toStrList(Object v) {
    if (!v) return List.of()
    if (v instanceof List) return v.collect { it.toString() }
    return List.of(v.toString())
  }
}
