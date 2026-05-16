package javasabr.mqtt.model.publish;

import java.util.UUID;

public interface IncomingPublish extends Publish {
  
  UUID id();

  @Override
  IncomingPublish withDuplicated();

  @Override
  IncomingPublish withoutRetained();
}
