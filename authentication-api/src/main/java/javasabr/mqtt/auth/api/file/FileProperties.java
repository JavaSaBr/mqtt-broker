package javasabr.mqtt.auth.api.file;

import java.net.URI;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;

public record FileProperties(URI path) {
  public FileProperties {
    if (path == null) {
      throw new AuthenticationConfigException("File path cannot be null");
    }
  }
}
