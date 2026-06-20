package javasabr.mqtt.acl.mug.dsl.loader


import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.mug.dsl.parser.GaclParser
import spock.lang.Specification

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
        def mockedRulesDefinition = "mock content"
        def stream = new ByteArrayInputStream(mockedRulesDefinition.getBytes())

    when:
        def rules = loader.load(stream)

    then:
        1 * parser.parse("mock content") >> []
        rules != null
  }
}
