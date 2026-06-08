package javasabr.mqtt.acl.java.dsl.antlr;

import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class AclSyntaxErrorListener extends BaseErrorListener {

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int position,
      String msg,
      RecognitionException e) {
    throw new AclConfigurationException("Syntax error at line %d, column %d: %s".formatted(line, position, msg), e);
  }
}
