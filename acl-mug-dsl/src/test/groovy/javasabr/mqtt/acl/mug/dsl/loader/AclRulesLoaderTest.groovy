package javasabr.mqtt.acl.mug.dsl.loader

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.mug.dsl.parser.GaclParser
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class AclRulesLoaderTest extends Specification {

  GaclParser parser
  AclRulesLoader loader

  def setup() {
    parser = Mock(GaclParser)
    loader = new AclRulesLoader(parser)
  }

  def "should throw exception when file does not exist"() {
    given:
        InputStream nonExistent = AclRulesLoaderTest.class.getResourceAsStream("non-existent.gacl")

    when:
        loader.load(nonExistent)

    then:
        thrown(AclConfigurationException)
  }

  def "should load rules successfully"() {
    given:
        def aclConfigMock = "mock content"
        def aclConfigInputStream = new ByteArrayInputStream(aclConfigMock.getBytes())

    when:
        def rules = loader.load(aclConfigInputStream)

    then:
        1 * parser.parse("mock content") >> []
        rules != null
  }

  def "should decode ACL config using UTF-8 charset"() {
    given:
        def nonAsciiContent = "user sënsor = pässwörd"
        def aclConfigInputStream = new ByteArrayInputStream(nonAsciiContent.getBytes(StandardCharsets.UTF_8))
    when:
        loader.load(aclConfigInputStream)
    then:
        1 * parser.parse(nonAsciiContent) >> []
  }

  def "should throw AclConfigurationException when input stream fails during read"() {
    given:
        def failingStream = new InputStream() {
          @Override
          int read() throws IOException {
            throw new IOException("simulated read failure")
          }
        }
    when:
        loader.load(failingStream)
    then:
        def exception = thrown(AclConfigurationException)
        exception.cause instanceof IOException
  }
}
