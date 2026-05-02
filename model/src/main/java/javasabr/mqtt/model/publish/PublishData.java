package javasabr.mqtt.model.publish;

import java.nio.ByteBuffer;
import java.util.UUID;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publish.impl.InMemoryPublishData;
import org.jspecify.annotations.Nullable;

public interface PublishData {
  
  static PublishData wrap(byte[] payload) {
    return new InMemoryPublishData(
        UUID.randomUUID(), 
        PayloadFormat.BINARY, 
        null,
        payload, 
        null);
  }

  static PublishData wrap(byte[] payload, byte[] correlationData) {
    return new InMemoryPublishData(
        UUID.randomUUID(),
        PayloadFormat.BINARY,
        null,
        payload,
        correlationData);
  }

  static PublishData wrap(byte[] payload, PayloadFormat format, byte[] correlationData) {
    return new InMemoryPublishData(
        UUID.randomUUID(),
        format,
        null,
        payload,
        correlationData);
  }
  
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
