package javasabr.mqtt.legacy.model;

public sealed interface Subscriber permits SingleSubscriber, SharedSubscriber {}
