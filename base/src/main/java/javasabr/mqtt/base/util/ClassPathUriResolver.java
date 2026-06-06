package javasabr.mqtt.base.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ClassPathUriResolver {

  private ClassPathUriResolver() {}

  public static Path resolveToPath(URI uri) {
    if (uri == null) {
      throw new NullPointerException("uri must not be null");
    }

    if ("classpath".equalsIgnoreCase(uri.getScheme())) {
      return resolveClasspathUri(uri);
    }

    return Path.of(uri);
  }

  private static Path resolveClasspathUri(URI uri) {
    String resourcePath = uri.getSchemeSpecificPart();
    if (resourcePath == null || resourcePath.isEmpty()) {
      throw new IllegalArgumentException(
          "classpath URI must have a non-empty resource path: " + uri);
    }

    if (resourcePath.startsWith("/")) {
      resourcePath = resourcePath.substring(1);
    }

    var classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = ClassPathUriResolver.class.getClassLoader();
    }

    var systemResource = classLoader.getResource(resourcePath);
    if (systemResource != null) {
      URI resourceUri = URI.create(systemResource.toString());
      if ("file".equalsIgnoreCase(resourceUri.getScheme())) {
        Path directPath = Path.of(resourceUri);
        if (Files.exists(directPath)) {
          return directPath;
        }
      }
    }

    Path localPath = Path.of(resourcePath);
    if (Files.exists(localPath)) {
      return localPath;
    }

    return extractToTempFile(classLoader, resourcePath);
  }

  private static Path extractToTempFile(ClassLoader classLoader, String resourcePath) {
    try (var inputStream = classLoader.getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IllegalArgumentException(
            "Classpath resource not found: " + resourcePath);
      }

      String fileName = extractFileName(resourcePath);
      String suffix = extractSuffix(fileName);

      Path tempFile = Files.createTempFile("classpath-" + fileName.replace('.', '_'), suffix);
      tempFile.toFile().deleteOnExit();
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      return tempFile;
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to extract classpath resource to temp file: " + resourcePath, e);
    }
  }

  private static String extractFileName(String resourcePath) {
    int lastSlash = resourcePath.lastIndexOf('/');
    return lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
  }

  private static String extractSuffix(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex >= 0 ? fileName.substring(dotIndex) : ".tmp";
  }
}
