package javasabr.mqtt.test.support

import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.concurrent.CompletionStage

class BaseSpecification extends Specification {

  protected <R> R fromAsync(Mono<R> mono) {
    return fromAsync(mono.toFuture())
  }

  protected <R> R fromAsync(CompletionStage<R> completionStage) {
    return completionStage.toCompletableFuture().join()
  }

  protected void waitForAsync(Mono<?> mono) {
    waitForAsync(mono.toFuture())
  }

  protected void waitForAsync(CompletionStage<?> completionStage) {
    completionStage.toCompletableFuture().join()
  }
}
