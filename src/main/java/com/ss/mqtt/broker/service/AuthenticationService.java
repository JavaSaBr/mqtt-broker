package com.ss.mqtt.broker.service;

import reactor.core.publisher.Mono;

public interface AuthenticationService {
  Mono<Boolean> auth(String userName, byte[] password);
}
