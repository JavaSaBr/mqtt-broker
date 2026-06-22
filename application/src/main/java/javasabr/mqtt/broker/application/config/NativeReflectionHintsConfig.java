package javasabr.mqtt.broker.application.config;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

/**
 * Registers array types that are reflectively instantiated at runtime (via {@code java.lang.reflect.Array.newInstance},
 * used by the rlib {@code Array} collections) so Spring AOT emits reflection hints for them. Kept as an AOT-processed
 * annotation instead of a handwritten {@code reachability-metadata.json} or a {@code RuntimeHintsRegistrar}.
 */
@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
    javasabr.mqtt.acl.engine.model.condition.MqttUserCondition[].class,
    javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher[].class,
    javasabr.mqtt.acl.engine.model.matcher.TopicMatcher[].class,
    javasabr.mqtt.acl.engine.model.rule.AclRule[].class,
    javasabr.mqtt.auth.api.AuthenticationProvider[].class,
    javasabr.mqtt.auth.api.CredentialsSource[].class,
    javasabr.mqtt.model.data.type.StringPair[].class,
    javasabr.mqtt.model.message.MqttMessageType[].class,
    javasabr.mqtt.model.MqttMessageProperty[].class,
    javasabr.mqtt.model.publish.IncomingPublish[].class,
    javasabr.mqtt.model.QoS[].class,
    javasabr.mqtt.model.reason.code.ConnectAckReasonCode[].class,
    javasabr.mqtt.model.reason.code.DisconnectReasonCode[].class,
    javasabr.mqtt.model.reason.code.PublishAckReasonCode[].class,
    javasabr.mqtt.model.reason.code.PublishCompletedReasonCode[].class,
    javasabr.mqtt.model.reason.code.PublishReceivedReasonCode[].class,
    javasabr.mqtt.model.reason.code.PublishReleaseReasonCode[].class,
    javasabr.mqtt.model.reason.code.SubscribeAckReasonCode[].class,
    javasabr.mqtt.model.subscriber.SingleSubscriber[].class,
    javasabr.mqtt.model.subscriber.Subscriber[].class,
    javasabr.mqtt.model.subscription.RequestedSubscription[].class,
    javasabr.mqtt.model.subscription.Subscription[].class,
    javasabr.mqtt.model.subscription.SubscriptionResult[].class,
    javasabr.mqtt.service.session.impl.ExpirableSession[].class,
    javasabr.mqtt.service.session.impl.InMemoryNetworkMqttSession[].class,
    javasabr.mqtt.service.session.impl.NotExpirableSession[].class,
    javasabr.rlib.network.packet.WritableNetworkPacket[].class,
    java.util.function.Consumer[].class,
    java.util.function.BiConsumer[].class,
    java.util.UUID[].class,
    reactor.core.publisher.FluxSink[].class,
    java.nio.ByteBuffer[].class,
    String[].class,
})
public class NativeReflectionHintsConfig {}
