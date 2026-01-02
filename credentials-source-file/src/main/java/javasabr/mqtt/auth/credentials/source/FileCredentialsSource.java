package javasabr.mqtt.auth.credentials.source;

import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javasabr.mqtt.auth.api.CredentialsSourceType;
import javasabr.mqtt.auth.api.InMemoryCredentialsSource;
import javasabr.mqtt.auth.api.exception.CredentialsSourceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCredentialsSource extends InMemoryCredentialsSource {

  URI fileName;

  public void init() {
    Path path = Path.of(fileName);
    if (!Files.exists(path)) {
      throw new CredentialsSourceException("Credentials file:[%s] could not be found".formatted(fileName));
    }
    try {
      reset(Files.newInputStream(path));
    } catch (IOException e) {
      throw new CredentialsSourceException("Error during credentials file read", e);
    }
  }

  @Override
  public CredentialsSourceType getCredentialsSourceType() {
    return CredentialsSourceType.FILE;
  }

  @Override
  public String toString() {
    return "{ \"credentialsSource\": \"%s\", \"filePath\": \"%s\" }".formatted(
        getCredentialsSourceType(),
        fileName.getPath());
  }
}
