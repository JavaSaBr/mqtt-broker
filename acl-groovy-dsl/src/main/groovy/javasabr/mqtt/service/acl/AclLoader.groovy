package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Rule
import lombok.AccessLevel
import lombok.experimental.FieldDefaults
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

@FieldDefaults(level = AccessLevel.PRIVATE)
class AclLoader {

  File file
  GroovyShell shell

  AclLoader() {
    file = getAclRulesFile()
    ImportCustomizer ic = new ImportCustomizer()
    ic.addStaticStars("javasabr.mqtt.model.acl.Permission")
    ic.addStaticStars("javasabr.mqtt.model.acl.Action")
    ic.addStaticStars("javasabr.mqtt.model.acl.Operator")
    CompilerConfiguration config = new CompilerConfiguration()
    config.addCompilationCustomizers(ic)
    shell = new GroovyShell(config)
  }

  private static File getAclRulesFile() {
    URL resource = AclLoader.class
        .getClassLoader()
        .getResource("acl.groovy")
    if (resource == null) {
      throw new IllegalStateException("Resource not loaded: acl.groovy")
    }
    File file;
    try {
      file = new File(resource.toURI())
    } catch (URISyntaxException e) {
      throw new RuntimeException(e)
    }
    if (!file.exists()) {
      throw new IllegalStateException("File not exists: acl.groovy")
    }
    return file;
  }

  List<Rule> load() {
    AclBuilder builder = new AclBuilder()
    shell.setVariable("rule", builder.&rule)
    shell.evaluate(file)
    builder.result()
  }
}

