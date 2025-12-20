package javasabr.mqtt.auth.credentials.source;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javasabr.mqtt.auth.api.CredentialsSourceException;
import javasabr.mqtt.auth.api.InMemoryCredentialSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCredentialsSource extends InMemoryCredentialSource {

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
  public String getName() {
    return "file";
  }

  @Override
  public String toString() {
    return "{ \"%s\": \"%s\" }".formatted(getName(), fileName.getPath());
  }
}
