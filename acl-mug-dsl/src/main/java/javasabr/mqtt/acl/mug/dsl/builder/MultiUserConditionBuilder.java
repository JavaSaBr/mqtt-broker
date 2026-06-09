package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition;
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition;
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition;

@SuppressWarnings("unchecked")
public abstract class MultiUserConditionBuilder<B extends MultiUserConditionBuilder<B>> extends
    UserConditionBuilder<B> {

  public B userNames(Consumer<UserMatchersBuilder> config) {
    UserMatchersBuilder builder = new UserMatchersBuilder();
    config.accept(builder);
    builder
        .build()
        .forEach(matcher -> conditions.add(new UserNameCondition(matcher)));
    return (B) this;
  }

  public B clientIds(Consumer<UserMatchersBuilder> config) {
    UserMatchersBuilder builder = new UserMatchersBuilder();
    config.accept(builder);
    builder
        .build()
        .forEach(matcher -> conditions.add(new ClientIdCondition(matcher)));
    return (B) this;
  }

  public B ipAddresses(Consumer<UserMatchersBuilder> config) {
    UserMatchersBuilder builder = new UserMatchersBuilder();
    config.accept(builder);
    builder
        .build()
        .forEach(matcher -> conditions.add(new IpAddressCondition(matcher)));
    return (B) this;
  }
}
