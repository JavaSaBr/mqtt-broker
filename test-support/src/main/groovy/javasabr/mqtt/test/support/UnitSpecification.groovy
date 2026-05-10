package javasabr.mqtt.test.support

import javasabr.rlib.common.util.ThreadUtils

class UnitSpecification extends BaseSpecification {

  protected static void waitUntil(long timeoutInMs, Closure<Boolean> condition) {
    long deadlineInMs = System.currentTimeMillis() + timeoutInMs
    while (System.currentTimeMillis() < deadlineInMs) {
      if (condition.call()) {
        return
      }
      ThreadUtils.sleep(10)
    }
    assert condition.call()
  }
}
