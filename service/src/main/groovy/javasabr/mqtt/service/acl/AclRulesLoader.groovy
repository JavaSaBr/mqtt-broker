package javasabr.mqtt.service.acl


import javasabr.mqtt.model.acl.Rule
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class AclRulesLoader {

  static List<Rule> load(String file) {
    if (!Objects.equals(file, "acl.groovy")) {
      return null;
    }
    def uri = AclRulesLoader.class.getClassLoader().getResource(file).toURI()
    evaluateDsl(uri)
  }

  private static List<Rule> evaluateDsl(URI file) {
    def ic = new ImportCustomizer()
    ic.addStaticStars('javasabr.mqtt.model.acl.Permission')
    ic.addStaticStars('javasabr.mqtt.model.acl.Action')
    ic.addStaticStars('javasabr.mqtt.model.acl.Operator')

    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(ic)

    def shell = new GroovyShell(config)
    def builder = new AclRulesBuilder()
    shell.setVariable("rule", builder.&rule)
    shell.evaluate(file)

    builder.result()
  }
}

