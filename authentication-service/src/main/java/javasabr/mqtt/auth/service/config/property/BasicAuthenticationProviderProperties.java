package javasabr.mqtt.auth.service.config.property;

import java.util.Map;
import javasabr.mqtt.auth.api.CredentialsSourceType;

public interface BasicAuthenticationProviderProperties extends SwitchableProperty{
  Map<CredentialsSourceType, CredentialsSourceProperties> credentialsSources();
}
