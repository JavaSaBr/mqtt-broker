//file:noinspection unused
package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Clients
import javasabr.mqtt.model.acl.Operator
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule

import static groovy.lang.Closure.DELEGATE_ONLY

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
      @DelegatesTo(strategy = DELEGATE_ONLY, value = RuleBuilder) Closure c) {
    def block = new RuleBuilder(name)
    c.delegate = block
    c.resolveStrategy = DELEGATE_ONLY
    c()
    root.rule << block.toMap()
    this
  }

  static class RuleBuilder {
    String name
    Permission permission
    Action action
    List<String> topics = []
    ClientsBuilder clients = new ClientsBuilder()

    RuleBuilder(String n) { name = n }

    RuleBuilder permission(Permission p) { permission = p; this }

    RuleBuilder action(Action a) { action = a; this }

    RuleBuilder topics(String... t) { topics.addAll(t); this }

    RuleBuilder clients(@DelegatesTo(strategy = DELEGATE_ONLY, value = ClientsBuilder) Closure c) {
      c.delegate = clients
      c.resolveStrategy = DELEGATE_ONLY
      c()
      this
    }

    Map toMap() {
      [
          name      : name,
          permission: permission,
          action    : action,
          topics    : topics,
          clients   : clients.toMap()
      ]
    }
  }

  static class ClientsBuilder {
    Operator operator = Operator.OR
    List<String> usernames = []
    List<String> clientIds = []
    List<String> clientAttrs = []
    List<String> ipAddresses = []

    ClientsBuilder operator(Operator op) { operator = op; this }

    ClientsBuilder username(String... u) { usernames.addAll(u); this }

    ClientsBuilder clientId(String... u) { clientIds.addAll(u); this }

    ClientsBuilder clientAttr(String... u) { clientAttrs.addAll(u); this }

    ClientsBuilder ipaddr(String... ips) { ipAddresses.addAll(ips); this }

    Map toMap() {
      [
          operator   : operator,
          usernames  : usernames,
          clientIds  : clientIds,
          clientAttrs: clientAttrs,
          ipAddresses: ipAddresses
      ]
    }
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
