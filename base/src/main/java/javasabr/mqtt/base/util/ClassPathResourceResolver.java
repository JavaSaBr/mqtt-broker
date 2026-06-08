package javasabr.mqtt.base.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassPathResourceResolver {

  private static final ClassLoader CLASS_LOADER = ClassPathResourceResolver.class.getClassLoader();

  public static InputStream newInputStream(URI uri) throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null");
    }
    switch (uri.getScheme()) {
      case "classpath":
        String sanitizedPath = uri.getSchemeSpecificPart();
        if (sanitizedPath == null || sanitizedPath.isEmpty()) {
          throw new IllegalArgumentException("Classpath URI must have a non-empty resource path: %s".formatted(uri));
        }
        Path resourcePath = Path.of(sanitizedPath);
        if (Files.exists(resourcePath)) {
          return Files.newInputStream(resourcePath);
        }
        if (sanitizedPath.startsWith("/")) {
          sanitizedPath = sanitizedPath.substring(1);
        }
        InputStream resourceAsStream = CLASS_LOADER.getResourceAsStream(sanitizedPath);
        if (resourceAsStream != null) {
          return resourceAsStream;
        }
        throw new FileNotFoundException(uri.toString());
      default:
        Path localPath = Path.of(uri);
        if (Files.exists(localPath)) {
          return Files.newInputStream(localPath);
        }
        throw new FileNotFoundException(uri.toString());
    }
  }
}
