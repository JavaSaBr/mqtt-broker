package javasabr.mqtt.model.message;

import javasabr.mqtt.model.data.type.StringPair;
import javasabr.rlib.collections.array.Array;

public interface MqttMessage {

  Array<StringPair> EMPTY_USER_PROPERTIES = Array.empty(StringPair.class);

  MqttMessageType messageType();
}
