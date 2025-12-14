package javasabr.mqtt.acl.service.conifg;

import java.net.URI;
import javasabr.mqtt.acl.service.impl.GroovyDslBasedAuthorizationService;
import javasabr.mqtt.service.AuthorizationService;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@CustomLog
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "acl.engine.type", havingValue = "groovy-dsl")
@ConditionalOnClass(name = "javasabr.mqtt.acl.groovy.dsl.loader.AclRulesLoader")
public class GroovyDslBasedAclServiceSpringConfig {

  @Bean
  AuthorizationService authorizationService(@Value("${acl.engine.groovy.dsl.config}") URI aclConfigUri) {
    log.info("Initializing Groovy-DSL based AuthorizationService...");
    var authorizationService = new GroovyDslBasedAuthorizationService();
    authorizationService.loadFrom(aclConfigUri);
    return authorizationService;
  }
}
