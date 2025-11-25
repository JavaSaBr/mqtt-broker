package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.ClientComparator
import javasabr.mqtt.model.acl.EqualsComparator

interface EqualsComparatorBuilder {

  default ClientComparator eq(String string) {
    new EqualsComparator(string)
  }
}
