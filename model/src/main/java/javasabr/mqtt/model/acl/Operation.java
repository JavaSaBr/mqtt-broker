package javasabr.mqtt.model.acl;

import java.util.function.Consumer;
import javasabr.rlib.collections.array.Array;
import org.jspecify.annotations.NonNull;

public enum Operation {
  PUBLISH,
  SUBSCRIBE;

  private static final Array<@NonNull Operation> CACHE = Array.of(values());

  public static void forEach(Consumer<Operation> action) {
    CACHE.forEach(action);
  }
}
