package javasabr.mqtt.model.publish;

public interface OutgoingPublish extends Publish {
  
  IncomingPublish source();

  @Override
  OutgoingPublish withDuplicated();
}
