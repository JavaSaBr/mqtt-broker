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
    if ("classpath".equals(uri.getScheme())) {
      String sanitizedPath = uri.getSchemeSpecificPart();
      InputStream resourceAsStream = CLASS_LOADER.getResourceAsStream(sanitizedPath.replaceFirst("^/", ""));
      if (resourceAsStream != null) {
        return resourceAsStream;
      }
      Path resourcePath = Path.of(sanitizedPath);
      if (Files.isDirectory(resourcePath)) {
        throw new IllegalArgumentException("Resource must be a file, not a directory:[%s]".formatted(uri));
      }
      if (Files.exists(resourcePath)) {
        return Files.newInputStream(resourcePath);
      }
    } else {
      Path localPath = Path.of(uri.getPath());
      if (Files.isDirectory(localPath)) {
        throw new IllegalArgumentException("Resource must be a file, not a directory:[%s]".formatted(uri));
      }
      if (Files.exists(localPath)) {
        return Files.newInputStream(localPath);
      }
    }
    throw new FileNotFoundException(uri.toString());
  }
}
