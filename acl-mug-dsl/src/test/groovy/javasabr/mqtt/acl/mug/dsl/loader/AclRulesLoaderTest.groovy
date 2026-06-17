package javasabr.mqtt.acl.mug.dsl.loader

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.mug.dsl.parser.GaclParser
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AclRulesLoaderTest extends Specification {

  GaclParser parser
  AclRulesLoader loader

  def setup() {
    parser = Mock(GaclParser)
    loader = new AclRulesLoader(parser)
  }

  def "should throw exception when file does not exist"() {
    given:
        Path nonExistent = Paths.get("non-existent.gacl")

    when:
        loader.load(nonExistent)

    then:
        thrown(AclConfigurationException)
  }

  def "should throw exception when file read fails"() {
    given:
        Path unreadableFile = Files.createTempFile("unreadable", ".gacl")
        // Make the file unreadable to trigger IOException during Files.readString
        unreadableFile.toFile().setReadable(false)

    when:
        loader.load(unreadableFile)

    then:
        thrown(AclConfigurationException)

    cleanup:
        unreadableFile.toFile().setReadable(true)
        Files.deleteIfExists(unreadableFile)
  }

  def "should load rules successfully"() {
    given:
        Path tempFile = Files.createTempFile("test", ".gacl")
        Files.writeString(tempFile, "mock content")

    and:
        parser.parse("mock content") >> []

    when:
        def rules = loader.load(tempFile)

    then:
        rules != null

    cleanup:
        Files.deleteIfExists(tempFile)
  }
}
