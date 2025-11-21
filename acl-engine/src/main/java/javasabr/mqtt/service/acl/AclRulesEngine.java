package javasabr.mqtt.service.acl;

import java.io.IOException;
import java.util.List;
import javasabr.mqtt.model.acl.Rule;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AclRulesEngine {

  List<Rule> rules;

  public AclRulesEngine() throws IOException {
    rules = new AclLoader().load();
  }

  public void authorize(String clientId) {

  }

}
