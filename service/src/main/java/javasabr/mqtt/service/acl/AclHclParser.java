package javasabr.mqtt.service.acl;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import java.io.InputStream;
import java.util.Map;

public class AclHclParser {

  public static Map<String, Object> parse(String file) {
    try (InputStream is = AclHclParser.class
        .getClassLoader()
        .getResourceAsStream(file)) {

      HCLParser parser = new HCLParser();
      return parser.parse(is);

    } catch (Exception e) {
      throw new RuntimeException("Failed to parse HCL ACL file", e);
    }
  }
}
