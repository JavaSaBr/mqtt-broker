package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.AclRoot
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class AclDslMapper {

  static AclRoot load(String file) {
    def uri = AclDslMapper.class.getClassLoader().getResource(file).toURI()
    return evaluateDsl(uri)
  }

  private static AclRoot evaluateDsl(URI file) {
    def ic = new ImportCustomizer()
    ic.addStaticStars('javasabr.mqtt.model.acl.Permission')
    ic.addStaticStars('javasabr.mqtt.model.acl.Action')
    ic.addStaticStars('javasabr.mqtt.model.acl.Operator')

    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(ic)

    def shell = new GroovyShell(config)
    def builder = new AclBuilder()
    shell.setVariable("acl", builder.&acl)
    shell.setVariable("user", builder.&user)
    shell.setVariable("group", builder.&group)
    shell.setVariable("rule", builder.&rule)
    shell.evaluate(file)

    return builder.result()
  }
}

