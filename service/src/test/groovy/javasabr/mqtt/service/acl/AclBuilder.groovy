package javasabr.mqtt.service.acl


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

  def acl(Closure c) {
    def m = [:]
    c.delegate = new BlockDelegate(m)
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    root.acl = m
  }

  def user(String name, Closure c) {
    root.user << buildNamedBlock(name, c)
  }

  def group(String name, Closure c) {
    root.group << buildNamedBlock(name, c)
  }

  def rule(String name, Closure c) {
    root.rule << buildNamedBlock(name, c)
  }

  private Map buildNamedBlock(String name, Closure c) {
    def m = [name: name]
    c.delegate = new BlockDelegate(m)
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c()
    return m
  }

  class BlockDelegate {
    Map store

    BlockDelegate(Map s) {
      store = s
    }

    def methodMissing(String name, args) {
      if (args.size() == 1 && args[0] instanceof Closure) {
        def child = [:]
        args[0].delegate = new BlockDelegate(child)
        args[0].resolveStrategy = Closure.DELEGATE_FIRST
        args[0]()
        store[name] = child
        return
      }
      if (args.size() == 1) {
        def v = args[0]
        if (v instanceof List)
          store[name] = v
        else
          store[name] = v
        return
      }
      throw new MissingMethodException(name, getClass(), args)
    }
  }

  private static List<User> mapUsers(List<Map> raw) {
    raw.collect { m ->
      new User(
          m.name,
          m.password ?: null,
          toStrList(m.groups)
      )
    }
  }

  private static List<Group> mapGroups(List<Map> raw) {
    raw.collect { m ->
      new Group(
          m.name,
          toStrList(m.users)
      )
    }
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
    if (m == null) return new RuleClients(List.of(), List.of())
    return new RuleClients(
        toStrList(m.users),
        toStrList(m.ipAddresses)
    )
  }

  private static List<String> toStrList(Object v) {
    if (v == null) return List.of()
    if (v instanceof List) return v.collect { it.toString() }
    return List.of(v.toString())
  }

  private static AclConfig mapAcl(Map m) {
    if (m == null)
      throw new IllegalStateException("acl{} block is required")
    def version = (m.version ?: 0) as int
    return new AclConfig(version)
  }
}
