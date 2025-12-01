package javasabr.mqtt.model.acl.condition;

import org.jspecify.annotations.Nullable;

public interface Condition<T> {

  boolean test(@Nullable T value);
}
