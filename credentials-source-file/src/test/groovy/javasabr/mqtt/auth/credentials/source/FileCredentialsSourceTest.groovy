package javasabr.mqtt.auth.credentials.source

import javasabr.mqtt.auth.api.exception.CredentialsSourceException
import spock.lang.Specification

class FileCredentialsSourceTest extends Specification {

  def "should report a missing credentials file as not found in the error message"() {
    given:
        def missingUri = URI.create("classpath:nonexistent/missing-credentials.properties")
    when:
        new FileCredentialsSource(missingUri)
    then:
        def exception = thrown(CredentialsSourceException)
        exception.cause instanceof FileNotFoundException
  }
}
