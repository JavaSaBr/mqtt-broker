package javasabr.mqtt.test.support

import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.concurrent.CompletionStage
import java.util.function.Supplier

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

  protected void waitUntil(Supplier<Boolean> function) {
    waitUntil(function, 50, 5000)
  }
  
  protected void waitUntil(Supplier<Boolean> function, int intervalInMs, int maxWaitingTimeInMs) {
    def deadline = System.currentTimeMillis() + maxWaitingTimeInMs
    while (System.currentTimeMillis() < deadline) {
      if (function.get()) {
        return
      }
      Thread.sleep(intervalInMs)
    }
    throw new IllegalStateException("Can't achieve the condition")
  }
}
