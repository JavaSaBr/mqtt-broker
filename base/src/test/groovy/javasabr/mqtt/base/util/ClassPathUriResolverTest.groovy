package javasabr.mqtt.base.util

import javasabr.mqtt.test.support.UnitSpecification

import java.nio.file.Files
import java.nio.file.Path

class ClassPathUriResolverTest extends UnitSpecification {

  def "should resolve file URI to Path"() {
    given:
      def tempFile = Files.createTempFile("test", ".txt")
      Files.writeString(tempFile, "hello")
      def uri = tempFile.toUri()
    when:
      def result = ClassPathUriResolver.resolveToPath(uri)
    then:
      Files.exists(result)
      Files.readString(result) == "hello"
    cleanup:
      Files.deleteIfExists(tempFile)
  }

  def "should resolve classpath URI to existing Path"() {
    given:
      def uri = URI.create("classpath:javasabr/mqtt/base/util/ClassPathUriResolver.class")
    when:
      def result = ClassPathUriResolver.resolveToPath(uri)
    then:
      Files.exists(result)
      Files.size(result) > 0
  }

  def "should handle classpath URI with leading slash"() {
    given:
      def uri = URI.create("classpath:/javasabr/mqtt/base/util/ClassPathUriResolver.class")
    when:
      def result = ClassPathUriResolver.resolveToPath(uri)
    then:
      Files.exists(result)
      Files.size(result) > 0
  }

  def "should throw NullPointerException for null URI"() {
    when:
      ClassPathUriResolver.resolveToPath(null)
    then:
      thrown(NullPointerException)
  }

  def "should throw IllegalArgumentException for missing classpath resource"() {
    given:
      def uri = URI.create("classpath:nonexistent/resource.txt")
    when:
      ClassPathUriResolver.resolveToPath(uri)
    then:
      thrown(IllegalArgumentException)
  }

  def "should throw IllegalArgumentException for classpath URI with blank resource path"() {
    given:
      def uri = new URI("classpath", " ", null)
    when:
      ClassPathUriResolver.resolveToPath(uri)
    then:
      thrown(IllegalArgumentException)
  }

  def "should resolve classpath URI via filesystem fallback when not on classloader"() {
    given:
      def testDir = Path.of("build/tmp/classpath-test")
      Files.createDirectories(testDir)
      def resourceFile = Files.writeString(testDir.resolve("data.txt"), "fallback content")
      def uri = URI.create("classpath:build/tmp/classpath-test/data.txt")
    when:
      def result = ClassPathUriResolver.resolveToPath(uri)
    then:
      Files.exists(result)
      Files.readString(result) == "fallback content"
    cleanup:
      Files.deleteIfExists(resourceFile)
      Files.deleteIfExists(testDir)
  }
}
