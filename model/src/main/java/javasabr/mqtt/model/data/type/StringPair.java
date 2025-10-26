package javasabr.mqtt.model.data.type;

import javasabr.mqtt.base.util.DebugUtils;

public record StringPair(String name, String value) {

  static {
    DebugUtils.registerIncludedFields("name", "value");
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
