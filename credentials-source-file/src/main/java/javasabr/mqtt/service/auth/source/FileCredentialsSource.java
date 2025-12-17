package javasabr.mqtt.service.auth.source;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javasabr.mqtt.model.exception.CredentialsSourceException;
import javasabr.rlib.collections.dictionary.DictionaryCollectors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCredentialsSource extends InMemoryCredentialSource {

  private static final String CREDENTIALS_SOURCE_NAME = "file";

  String fileName;

  public FileCredentialsSource(String fileName) {
    this.fileName = fileName;
    init();
  }

  @Override
  void init() {
    URL credentialUrl = FileCredentialsSource.class
        .getClassLoader()
        .getResource(fileName);

    if (credentialUrl == null) {
      throw new CredentialsSourceException("Credentials file:[%s] could not be found".formatted(fileName));
    }

    try {
      var credentialsProperties = new Properties();
      credentialsProperties.load(new FileInputStream(credentialUrl.getPath()));

      var credentials = credentialsProperties
          .entrySet()
          .stream()
          .collect(DictionaryCollectors.toRefToRefDictionary(
              entry -> entry.getKey().toString(),
              entry -> entry.getValue().toString().getBytes(StandardCharsets.UTF_8)));

      reset(credentials);
    } catch (IOException e) {
      throw new CredentialsSourceException(e);
    }
  }

  @Override
  public String getName() {
    return CREDENTIALS_SOURCE_NAME;
  }
}
