package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.FileProperties;
import javasabr.mqtt.auth.service.config.property.SpringFileProperties;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.FileCredentialsSource")
@ConditionalOnProperty(name = "authentication.credentials-source.file.enabled", havingValue = "true")
@EnableConfigurationProperties(SpringFileProperties.class)
public class FileCredentialsSourceSpringConfig {

  @Bean(initMethod = "init")
  FileCredentialsSource fileCredentialsSource(FileProperties fileCredentialsSourceProperties) {
    return new FileCredentialsSource(fileCredentialsSourceProperties.path());
  }
}
