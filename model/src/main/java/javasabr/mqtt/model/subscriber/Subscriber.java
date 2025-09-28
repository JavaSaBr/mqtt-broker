package javasabr.mqtt.model.subscriber;

public sealed interface Subscriber permits SingleSubscriber, SharedSubscriber {}
