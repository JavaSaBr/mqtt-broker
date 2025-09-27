package javasabr.mqtt.legacy.model.reason.code;

import java.util.stream.Stream;
import javasabr.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthenticateReasonCode {

  /**
   * Authentication is successful. Server.
   */
  SUCCESS((byte) 0x00),
  /**
   * Continue the authentication with another step. Client or Server.
   */
  CONTINUE_AUTHENTICATION((byte) 0x18),
  /**
   * Initiate a re-authentication. Client.
   */
  RE_AUTHENTICATE((byte) 0x19);

  private static final AuthenticateReasonCode[] VALUES;

  static {

    var maxId = Stream
        .of(values())
        .mapToInt(AuthenticateReasonCode::getValue)
        .max()
        .orElse(0);

    var values = new AuthenticateReasonCode[maxId + 1];

    for (var value : values()) {
      values[value.value] = value;
    }

    VALUES = values;
  }

  public static AuthenticateReasonCode of(int index) {
    return ObjectUtils.notNull(
        VALUES[index],
        index,
        arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg));
  }

  private @Getter
  final byte value;
}
