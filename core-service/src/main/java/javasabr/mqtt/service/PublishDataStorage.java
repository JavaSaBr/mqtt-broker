package javasabr.mqtt.service;

import java.util.UUID;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publish.PublishData;
import org.jspecify.annotations.Nullable;

public interface PublishDataStorage {

  @Nullable
  PublishData findById(UUID dataId);
  
  void store(PublishData publishData);

  PublishData store(
      UUID dataId,
      @Nullable String contentType,
      PayloadFormat payloadFormat,
      byte[] payload,
      byte @Nullable [] correlationData);
  
  void removeById(UUID dataId);
}
