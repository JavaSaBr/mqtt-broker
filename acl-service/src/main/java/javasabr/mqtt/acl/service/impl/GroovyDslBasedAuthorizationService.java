package javasabr.mqtt.acl.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import javasabr.mqtt.acl.engine.AclEngine;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.groovy.dsl.loader.AclRulesLoader;
import javasabr.mqtt.acl.service.AclEngineBasedAuthorizationService;
import javasabr.mqtt.base.util.ClassPathResourceResolver;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import lombok.CustomLog;

@CustomLog
public class GroovyDslBasedAuthorizationService extends AclEngineBasedAuthorizationService {

  public void loadFrom(URI resource) {
    try (InputStream aclInputStream =  ClassPathResourceResolver.newInputStream(resource)) {
      Map<Operation, Array<AclRule>> loadedAclRulesMap = AclRulesLoader.load(aclInputStream);
      switchTo(new AclEngine(loadedAclRulesMap));
      log.info(resource, loadedAclRulesMap, GroovyDslBasedAuthorizationService::buildServiceDescription);
    } catch (IOException e) {
      throw new AclConfigurationException("ACL configuration issue:[%s]".formatted(resource), e);
    }
  }

  private static String buildServiceDescription(URI resource, Map<Operation, Array<AclRule>> aclRulesMap) {
    var builder = new StringBuilder();
    builder.append("{\n");
    builder.append("  \"SOURCE\":\"").append(resource).append("\",\n");
    
    int count = 0;
    for (Map.Entry<Operation, Array<AclRule>> entry : aclRulesMap.entrySet()) {
      Operation operation = entry.getKey();
      Array<AclRule> rules = entry.getValue();
      count += rules.size();
      builder
          .append("  \"")
          .append(operation.name())
          .append("\": ")
          .append(rules.size())
          .append(",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Loaded total [%s] ACL rules: %s".formatted(count, builder);
  }
}
