package javasabr.mqtt.service.acl

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import javasabr.mqtt.model.acl.AclConfig
import javasabr.mqtt.model.acl.AclRoot
import javasabr.mqtt.model.acl.Group
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.RuleClients
import javasabr.mqtt.model.acl.User

class AclBuilder {

  private Map root = [
      acl  : null,
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

  def invokeMethod(String name, args) {
    if (name in ['acl', 'user', 'group', 'rule'] && args) {
      this."$name"(*args)
    } else {
      throw new MissingMethodException(name, this.class, args)
    }
  }

  // ---------------------------
  // ACL block
  // ---------------------------
  void acl(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AclBlock) Closure c) {
    def block = new AclBlock()
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.acl = block.toMap()
  }

  // ---------------------------
  // Named blocks
  // ---------------------------
  void user(
      String name,
      @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = UserBlock)
      @ClosureParams(FirstParam.FirstGenericType) Closure<?> c
  ) {
    def block = new UserBlock(name)
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c(block)
    root.user << block.toMap()
  }

  void group(String name, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GroupBlock) Closure c) {
    def block = new GroupBlock(name)
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.group << block.toMap()
  }

  void rule(String name, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = RuleBlock) Closure c) {
    def block = new RuleBlock(name)
    c.delegate = block
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.rule << block.toMap()
  }

  // ---------------------------
  // DSL block classes
  // ---------------------------
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

    void groups(String... gs) { if (gs) groups.addAll(gs as List) }

    void groups(Collection<String> gs) { if (gs) groups.addAll(gs) }

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
    int priority
    String effect
    String event
    List<String> topics = []
    ClientsBlock clients = new ClientsBlock()

    RuleBlock(String n) { name = n }

    void priority(int p) { priority = p }

    void effect(String e) { effect = e }

    void event(String ev) { event = ev }

    void topics(String... t) { topics.addAll(t) }

    void clients(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ClientsBlock) Closure c) {
      c.delegate = clients
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c()
    }

    Map toMap() {
      [name: name, priority: priority, effect: effect, event: event, topics: topics, clients: clients.toMap()]
    }
  }

  static class ClientsBlock {
    List<String> users = []
    List<String> ipAddresses = []

    void users(String... u) { users.addAll(u) }

    void ipAddresses(String... ips) { ipAddresses.addAll(ips) }

    Map toMap() { [users: users, ipAddresses: ipAddresses] }
  }

  // ---------------------------
  // Mapping to Java records
  // ---------------------------
  private static List<User> mapUsers(List<Map> raw) {
    raw.collect { m -> new User(m.name, m.password ?: null, toStrList(m.groups)) }
  }

  private static List<Group> mapGroups(List<Map> raw) {
    raw.collect { m -> new Group(m.name, toStrList(m.users)) }
  }

  private static List<Rule> mapRules(List<Map> raw) {
    raw.collect { m ->
      new Rule(
          m.name,
          (m.priority ?: 0) as int,
          m.effect,
          m.event,
          mapClients(m.clients),
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
