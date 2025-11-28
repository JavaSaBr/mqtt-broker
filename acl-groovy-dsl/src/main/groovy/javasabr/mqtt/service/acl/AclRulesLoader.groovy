package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.mqtt.service.acl.builder.AclRulesBuilder
import javasabr.rlib.collections.array.Array
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.file.Files
import java.nio.file.Path

import static java.util.stream.Collectors.collectingAndThen
import static java.util.stream.Collectors.groupingBy
import static java.util.stream.Collectors.toCollection
import static javasabr.rlib.collections.array.ArrayFactory.mutableArray

class AclRulesLoader {

  private final Path aclConfigPath

  AclRulesLoader(String aclConfigPath) {
    this.aclConfigPath = Path.of(aclConfigPath)
    if (Files.notExists(this.aclConfigPath)) {
      throw new AclConfigurationException("Class loader unable to load resource: %s".formatted(this.aclConfigPath))
    }
  }

  EnumMap<Operation, Array<Rule>> load() {
    CompilerConfiguration compilerConfig = new CompilerConfiguration()
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()
    new GroovyShell(compilerConfig).with {
      setVariable("allowPublish", aclRulesBuilder.&allowPublish)
      setVariable("denyPublish", aclRulesBuilder.&denyPublish)
      setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
      setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
      evaluate(aclConfigPath.toFile())
    }
    Map<Operation, Array<Rule>> map = aclRulesBuilder.build()
        .stream()
        .collect(groupingBy(
            Rule::operation,
            { new LinkedHashMap<Operation, Array<Rule>>() },
            collectingAndThen(toCollection(() -> mutableArray(Rule.class)), Array::copyOf)));
    return new EnumMap<>(map)
  }
}
