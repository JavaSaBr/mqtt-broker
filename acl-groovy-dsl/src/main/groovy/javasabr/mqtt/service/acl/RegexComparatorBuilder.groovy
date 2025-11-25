package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.ClientComparator
import javasabr.mqtt.model.acl.RegexComparator

import java.util.regex.Pattern

interface RegexComparatorBuilder {

  default ClientComparator regex(String string) {
    new RegexComparator(Pattern.compile(string))
  }
}
