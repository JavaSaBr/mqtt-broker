package javasabr.mqtt.legacy.service.impl;

import javasabr.mqtt.model.exception.CredentialsSourceException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javasabr.rlib.collections.dictionary.DictionaryCollectors;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;

public class FileCredentialsSource extends AbstractCredentialSource {

  private final String fileName;

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
      throw new CredentialsSourceException("Credentials file could not be found");
    }

    try {
      var credentialsProperties = new Properties();
      credentialsProperties.load(new FileInputStream(credentialUrl.getPath()));

      RefToRefDictionary<String, byte[]> credentials = credentialsProperties
          .entrySet()
          .stream()
          .collect(DictionaryCollectors.toRefToRefDictionary(
              entry -> entry.getKey().toString(),
              entry -> entry.getValue().toString().getBytes(StandardCharsets.UTF_8)));

      putAll(credentials);
    } catch (IOException e) {
      throw new CredentialsSourceException(e);
    }
  }
}
