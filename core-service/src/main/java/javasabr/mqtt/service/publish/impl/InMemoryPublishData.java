package javasabr.mqtt.service.publish.impl;

import java.nio.ByteBuffer;
import java.util.UUID;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.rlib.common.util.ArrayUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

@Getter
@Accessors
public class InMemoryPublishData implements PublishData {
  
  UUID id;
  PayloadFormat payloadFormat;
  @Nullable
  String contentType;
  
  int payloadSize;
  int correlationDataSize;
  
  byte[] payload;
  byte @Nullable[] correlationData;

  public InMemoryPublishData(
      UUID id,
      PayloadFormat payloadFormat,
      @Nullable String contentType,
      byte[] payload,
      byte @Nullable [] correlationData) {
    this.id = id;
    this.contentType = contentType;
    this.payloadFormat = payloadFormat;
    this.payload = payload;
    this.payloadSize = payload.length;
    this.correlationData = correlationData;
    this.correlationDataSize = ArrayUtils.length(correlationData);
  }

  @Override
  public boolean isPayloadEmpty() {
    return payloadSize < 1;
  }

  @Override
  public boolean isCorrelationDataEmpty() {
    return correlationDataSize < 1;
  }

  @Override
  public void writePayloadTo(ByteBuffer buffer) {
    buffer.put(payload);
  }

  @Override
  public void writeCorrelationDataTo(ByteBuffer buffer) {
    if (correlationData != null) {
      buffer.put(correlationData);
    }
  }
}
