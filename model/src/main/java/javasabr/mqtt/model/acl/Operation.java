package javasabr.mqtt.model.acl;

import java.util.EnumSet;
import java.util.function.Consumer;

public enum Operation {
  PUBLISH,
  SUBSCRIBE;

  private static final EnumSet<Operation> CACHE = EnumSet.allOf(Operation.class);

  public static void forEach(Consumer<Operation> action) {
    CACHE.forEach(action);
  }
}
