package javasabr.mqtt.broker.application.config;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class NativeConfigurationHints implements RuntimeHintsRegistrar {

  private static final String[] JDK_ARRAY_TYPES = {
      "javasabr.mqtt.acl.engine.model.condition.MqttUserCondition[]",
      "javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher[]",
      "javasabr.mqtt.acl.engine.model.matcher.TopicMatcher[]",
      "javasabr.mqtt.acl.engine.model.rule.AclRule[]",
      "javasabr.mqtt.auth.api.AuthenticationProvider[]",
      "javasabr.mqtt.auth.api.CredentialsSource[]",
      "javasabr.mqtt.model.data.type.StringPair[]",
      "javasabr.mqtt.model.message.MqttMessageType[]",
      "javasabr.mqtt.model.MqttMessageProperty[]",
      "javasabr.mqtt.model.publish.IncomingPublish[]",
      "javasabr.mqtt.model.QoS[]",
      "javasabr.mqtt.model.reason.code.ConnectAckReasonCode[]",
      "javasabr.mqtt.model.reason.code.DisconnectReasonCode[]",
      "javasabr.mqtt.model.reason.code.PublishAckReasonCode[]",
      "javasabr.mqtt.model.reason.code.PublishCompletedReasonCode[]",
      "javasabr.mqtt.model.reason.code.PublishReceivedReasonCode[]",
      "javasabr.mqtt.model.reason.code.PublishReleaseReasonCode[]",
      "javasabr.mqtt.model.reason.code.SubscribeAckReasonCode[]",
      "javasabr.mqtt.model.subscriber.SingleSubscriber[]",
      "javasabr.mqtt.model.subscriber.Subscriber[]",
      "javasabr.mqtt.model.subscription.RequestedSubscription[]",
      "javasabr.mqtt.model.subscription.Subscription[]",
      "javasabr.mqtt.model.subscription.SubscriptionResult[]",
      "javasabr.mqtt.service.session.impl.ExpirableSession[]",
      "javasabr.mqtt.service.session.impl.InMemoryNetworkMqttSession[]",
      "javasabr.mqtt.service.session.impl.NotExpirableSession[]",
      "javasabr.rlib.network.packet.WritableNetworkPacket[]",
      "java.util.function.Consumer[]",
      "java.util.function.BiConsumer[]",
      "java.util.UUID[]",
      "reactor.core.publisher.FluxSink[]",
      "java.nio.ByteBuffer[]",
      "String[]",
      };

  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    for (String arrayType : JDK_ARRAY_TYPES) {
      hints
          .reflection()
          .registerType(TypeReference.of(arrayType));
    }
  }
}
