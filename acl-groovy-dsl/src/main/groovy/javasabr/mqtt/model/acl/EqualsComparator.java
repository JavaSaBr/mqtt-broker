package javasabr.mqtt.model.acl;

import java.util.Objects;

public record EqualsComparator(String rulePattern) implements ClientComparator {
  @Override
  public boolean compare(String b) {
    return Objects.equals(rulePattern, b);
  }
}
