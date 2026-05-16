package javasabr.mqtt.model.publish.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.rlib.common.util.ArrayUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryPublishData implements PublishData {
  
  static {
    DebugUtils.registerIncludedFields("id", "payloadSize", "correlationDataSize");
  }
  
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
    //noinspection DataFlowIssue caller should check it
    buffer.put(correlationData);
  }

  @JsonValue
  public Object jsonDebugValue() {
    return Map.of(
        "id", id, 
        "payloadSize", payloadSize, 
        "correlationDataSize", correlationDataSize);
  }
  
  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
