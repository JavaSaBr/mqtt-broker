package javasabr.mqtt.acl.engine.model.condition;

import org.jspecify.annotations.Nullable;

public interface Condition<T> {

  boolean test(@Nullable T value);
}
