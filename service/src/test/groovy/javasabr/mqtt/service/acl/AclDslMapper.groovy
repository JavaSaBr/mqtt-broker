package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.AclRoot

class AclDslMapper {

  static AclRoot load(String file) {
    def uri = AclDslMapper.class.getClassLoader().getResource(file).toURI()
    return evaluateDsl(uri)
  }

  private static AclRoot evaluateDsl(URI file) {
    def shell = new GroovyShell()
    def builder = new AclBuilder()
    shell.setVariable("acl", builder.&acl)
    shell.setVariable("user", builder.&user)
    shell.setVariable("group", builder.&group)
    shell.setVariable("rule", builder.&rule)
    shell.evaluate(file)
    return builder.result()
  }
}

