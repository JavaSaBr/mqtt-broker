package javasabr.mqtt.model.exception;

class AclConfigurationException extends RuntimeException {

  AclConfigurationException(String message) {
    super(message);
  }

  AclConfigurationException(Throwable cause) {
    super(cause);
  }
}
