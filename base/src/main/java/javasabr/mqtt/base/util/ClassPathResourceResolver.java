package javasabr.mqtt.base.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClassPathResourceResolver {

  private ClassPathResourceResolver() {}

  public static InputStream newInputStream(URI uri) throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null");
    }
    if ("classpath".equalsIgnoreCase(uri.getScheme())) {
      String resourcePath = uri.getSchemeSpecificPart();
      if (resourcePath == null || resourcePath.isEmpty()) {
        throw new IllegalArgumentException("Classpath URI must have a non-empty resource path: %s".formatted(uri));
      }
      Path localPath = Path.of(resourcePath);
      if (Files.exists(localPath)) {
        return Files.newInputStream(localPath);
      }
      throw new FileNotFoundException(uri.toString());
    }
    Path localPath = Path.of(uri);
    if (Files.exists(localPath)) {
      return Files.newInputStream(localPath);
    }
    throw new FileNotFoundException(uri.toString());
  }
}
