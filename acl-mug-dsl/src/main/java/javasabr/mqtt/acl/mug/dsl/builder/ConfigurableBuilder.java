package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;

/**
 * Base class for configurable builders.
 *
 * @param <B> the type of the builder.
 */
public abstract class ConfigurableBuilder<B extends ConfigurableBuilder<B>> {

  /**
   * Applies the configuration to this builder.
   *
   * @param config the configuration consumer.
   * @return this builder.
   */
  @SuppressWarnings("unchecked")
  public B apply(Consumer<B> config) {
    config.accept((B) this);
    return (B) this;
  }
}
