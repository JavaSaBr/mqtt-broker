package com.ss.mqtt.broker.model;

public sealed interface Subscriber permits SingleSubscriber, SharedSubscriber {}
