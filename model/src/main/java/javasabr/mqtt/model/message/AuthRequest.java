package javasabr.mqtt.model.message;

public interface AuthRequest {

  String username();

  byte[] password();

  String authenticationMethod();

  byte[] authenticationData();
}
