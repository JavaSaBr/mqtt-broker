package javasabr.mqtt.base.util

import javasabr.mqtt.test.support.UnitSpecification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ClassPathResourceResolverTest extends UnitSpecification {

  def "should resolve file URI to Path"() {
    given:
        def tempFile = Files.createTempFile("test", ".txt")
        Files.writeString(tempFile, "hello")
        def uri = tempFile.toUri()
    when:
        def result = ClassPathResourceResolver.newInputStream(uri)
    then:
        new String(result.readAllBytes(), StandardCharsets.UTF_8) == "hello"
    cleanup:
        result.close()
        Files.deleteIfExists(tempFile)
  }

  def "should resolve classpath URI to existing Path"() {
    given:
        def uri = URI.create("classpath:javasabr/mqtt/base/util/ClassPathResourceResolver.class")
    when:
        def result = ClassPathResourceResolver.newInputStream(uri)
    then:
        result.available() > 0
    cleanup:
        result.close()
  }

  def "should handle classpath URI with leading slash"() {
    given:
        def uri = URI.create("classpath:/javasabr/mqtt/base/util/ClassPathResourceResolver.class")
    when:
        def result = ClassPathResourceResolver.newInputStream(uri)
    then:
        result.available() > 0
    cleanup:
        result.close()
  }

  def "should throw IllegalArgumentException for null URI"() {
    when:
        ClassPathResourceResolver.newInputStream(null)
    then:
        thrown(IllegalArgumentException)
  }

  def "should throw FileNotFoundException for missing classpath resource"() {
    given:
        def uri = URI.create("classpath:nonexistent/resource.txt")
    when:
        ClassPathResourceResolver.newInputStream(uri)
    then:
        thrown(FileNotFoundException)
  }

  def "should throw FileNotFoundException for classpath URI with blank resource path"() {
    given:
        def uri = new URI("classpath", " ", null)
    when:
        ClassPathResourceResolver.newInputStream(uri)
    then:
        thrown(FileNotFoundException)
  }

  def "should resolve classpath URI via filesystem fallback when not on classloader"() {
    given:
        def testDir = Path.of("build/tmp/classpath-test")
        Files.createDirectories(testDir)
        def resourceFile = Files.writeString(testDir.resolve("data.txt"), "fallback content")
        def uri = URI.create("classpath:build/tmp/classpath-test/data.txt")
    when:
        def result = ClassPathResourceResolver.newInputStream(uri)
    then:
        new String(result.readAllBytes(), StandardCharsets.UTF_8) == "fallback content"
    cleanup:
        result.close()
        Files.deleteIfExists(resourceFile)
        Files.deleteIfExists(testDir)
  }

  def "should reject directory URI instead of returning an empty stream"() {
    given:
        def tempDir = Files.createTempDirectory("resolver-directory")
        def uri = tempDir.toUri()
    when:
        ClassPathResourceResolver.newInputStream(uri)
    then:
        def exception = thrown(IllegalArgumentException)
        exception.message != null
        exception.message.toLowerCase().contains("directory")
    cleanup:
        Files.deleteIfExists(tempDir)
  }

  def "should resolve schemeless file URI without NullPointerException"() {
    given:
        def tempFile = Files.createTempFile("schemeless", ".txt")
        Files.writeString(tempFile, "schemeless-content")
        def uri = URI.create(tempFile.toString())
    when:
        def result = ClassPathResourceResolver.newInputStream(uri)
    then:
        new String(result.readAllBytes(), StandardCharsets.UTF_8) == "schemeless-content"
    cleanup:
        result?.close()
        Files.deleteIfExists(tempFile)
  }

  def "should prefer classpath resource over a same-named file in the working directory"() {
    given:
        def workingDirShadow = Path.of("cpr-precedence.txt")
        Files.writeString(workingDirShadow, "from-working-directory")
        def uri = URI.create("classpath:cpr-precedence.txt")
    when:
        def result = ClassPathResourceResolver.newInputStream(uri)
    then:
        new String(result.readAllBytes(), StandardCharsets.UTF_8) == "from-classpath\n"
    cleanup:
        result?.close()
        Files.deleteIfExists(workingDirShadow)
  }
}
