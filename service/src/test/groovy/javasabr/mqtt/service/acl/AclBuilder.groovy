//file:noinspection unused
package javasabr.mqtt.service.acl

import groovy.transform.CompileStatic
import javasabr.mqtt.model.acl.AclConfig
import javasabr.mqtt.model.acl.AclRoot
import javasabr.mqtt.model.acl.Group
import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operator
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.RuleClients
import javasabr.mqtt.model.acl.User

/**
 * Builds {@link javasabr.mqtt.model.acl.AclRoot} from ACL configuration
 */
class AclBuilder {

  private Map root = [
      acl  : [:],
      user : [],
      group: [],
      rule : []
  ]

  AclRoot result() {
    return new AclRoot(
        mapAcl(root.acl),
        mapUsers(root.user),
        mapGroups(root.group),
        mapRules(root.rule)
    )
  }

  /*def invokeMethod(String name, args) {
    if (name in ['acl', 'user', 'group', 'rule'] && args) {
      this."$name"(*args)
    } else {
      throw new MissingMethodException(name, this.class, args)
    }
  }*/

  /**
   * Defines ACL metadata
   *
   * @param version of ACL config
   * @return
   */
  AclBuilder acl(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AclBlock) Closure c) {
    def block = new AclBlock()
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.acl = block.toMap()
    this
  }

  AclBuilder user(
      String name,
      @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = UserBlock) Closure<?> c) {
    def block = new UserBlock(name)
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c(block)
    root.user << block.toMap()
    this
  }

  AclBuilder group(
      String name,
      @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GroupBlock) Closure c) {
    def block = new GroupBlock(name)
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.group << block.toMap()
    this
  }

  AclBuilder rule(
      String name,
      @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = RuleBlock) Closure c) {
    def block = new RuleBlock(name)
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.rule << block.toMap()
    this
  }

  static class AclBlock {
    int version = 0

    void version(int v) { version = v }

    Map toMap() { [version: version] }
  }

  @CompileStatic
  static class UserBlock {
    String name
    String password
    List<String> groups = []

    UserBlock(String n) { name = n }

    void password(String pw) { password = pw }

    void groups(String... gs) { groups.addAll(gs) }

    Map toMap() { [name: name, password: password, groups: groups] }
  }

  static class GroupBlock {
    String name
    List<String> users = []

    GroupBlock(String n) { name = n }

    void users(String... u) { users.addAll(u) }

    Map toMap() { [name: name, users: users] }
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

    void clients(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ClientsBlock) Closure c) {
      c.delegate = clients
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c()
    }

    Map toMap() {
      [name: name, permission: permission, action: action, topics: topics, clients: clients.toMap()]
    }
  }

  static class ClientsBlock {
    Operator operator = Operator.OR
    List<String> users = []
    List<String> ipAddresses = []

    void operator(Operator op) { operator = op }

    void users(String... u) { users.addAll(u) }

    void ipAddresses(String... ips) { ipAddresses.addAll(ips) }

    Map toMap() { [users: users, ipAddresses: ipAddresses] }
  }

  private static List<User> mapUsers(List<Map<String, String>> raw) {
    raw.collect { m -> new User(m.name, m.password ?: null, toStrList(m.groups)) }
  }

  private static List<Group> mapGroups(List<Map<String, String>> raw) {
    raw.collect { m -> new Group(m.name, toStrList(m.users)) }
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

  private static RuleClients mapClients(Map m) {
    if (!m) return new RuleClients(List.of(), List.of())
    return new RuleClients(toStrList(m.users), toStrList(m.ipAddresses))
  }

  private static List<String> toStrList(Object v) {
    if (!v) return List.of()
    if (v instanceof List) return v.collect { it.toString() }
    return List.of(v.toString())
  }

  private static AclConfig mapAcl(Map m) {
    if (!m) throw new IllegalStateException("acl{} block is required")
    new AclConfig((m.version ?: 0) as int)
  }
}
