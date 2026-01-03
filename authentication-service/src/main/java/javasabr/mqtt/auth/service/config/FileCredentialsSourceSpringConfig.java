package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.property.CredentialsSourceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.FileCredentialsSource")
@ConditionalOnProperty(name = "authentication.credentials-source.file.enabled", havingValue = "true")
public class FileCredentialsSourceSpringConfig {
  @Bean
  CredentialsSourceProperties fileCredentialsSourceProperties(AuthenticationProperties authenticationProperties) {
    return authenticationProperties.credentialsSource().get(CredentialsSourceType.FILE);
  }

  @Bean(initMethod = "init")
  FileCredentialsSource fileCredentialsSource(CredentialsSourceProperties fileCredentialsSourceProperties) {
    return new FileCredentialsSource(fileCredentialsSourceProperties.uriPath());
  }
}
