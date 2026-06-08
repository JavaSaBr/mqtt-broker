package javasabr.mqtt.auth.credentials.source;

import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.api.exception.CredentialsSourceException;
import javasabr.mqtt.base.util.ClassPathResourceResolver;
import javasabr.mqtt.base.util.DebugUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCredentialsSource extends InMemoryCredentialsSource {

  URI fileName;

  public FileCredentialsSource(URI fileName) {
    this.fileName = fileName;
    init();
  }

  private void init() {
    try {
      reset(ClassPathResourceResolver.newInputStream(fileName));
    } catch (IOException e) {
      throw new CredentialsSourceException("Error during credentials file read", e);
    }
  }

  @Override
  public CredentialsSourceType getType() {
    return CredentialsSourceType.FILE;
  }
  
  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of(
        "type", getType(),
        "filePath", fileName);
  }
}
