package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.ClientComparator
import javasabr.mqtt.model.acl.ClientMatcher
import javasabr.rlib.collections.array.Array

import java.util.function.Function

interface ClientMatcherBuilder {

  default ClientMatcher match(Function<CallId, String> valueGetter,
                              Array<ClientComparator> ruleMatchers) {
    new ClientMatcher(valueGetter, ruleMatchers)
  }
}
