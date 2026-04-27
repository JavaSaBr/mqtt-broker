package javasabr.mqtt.model.publish;

import java.nio.ByteBuffer;
import java.util.UUID;
import javasabr.mqtt.model.PayloadFormat;
import org.jspecify.annotations.Nullable;

public interface PublishData {
  
  UUID id();

  @Nullable
  String contentType();
  PayloadFormat payloadFormat();

  int payloadSize();
  int correlationDataSize();
  
  boolean isPayloadEmpty();
  boolean isCorrelationDataEmpty();
  
  void writePayloadTo(ByteBuffer buffer);
  void writeCorrelationDataTo(ByteBuffer buffer);
}
