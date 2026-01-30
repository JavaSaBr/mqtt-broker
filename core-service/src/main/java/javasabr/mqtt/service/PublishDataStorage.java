package javasabr.mqtt.service;

import java.util.UUID;
import javasabr.mqtt.model.publish.PublishData;

public interface PublishDataStorage {
  
  void store(PublishData publishData);
  
  void remove(UUID dataId);
}
