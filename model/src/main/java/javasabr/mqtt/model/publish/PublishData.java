package javasabr.mqtt.model.publish;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface PublishData {
  
  UUID id();
  
  long size();
  
  void writeTo(ByteBuffer buffer);
}
