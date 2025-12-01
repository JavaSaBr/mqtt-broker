package javasabr.mqtt.model.reason.code;

import javasabr.rlib.common.util.NumberedEnum;
import javasabr.rlib.common.util.NumberedEnumMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors
@RequiredArgsConstructor
public enum AuthenticateReasonCode implements NumberedEnum<AuthenticateReasonCode>, ReasonCode {

  /**
   * Authentication is successful. Server.
   */
  SUCCESS(0x00),
  /**
   * Continue the authentication with another step. Client or Server.
   */
  CONTINUE_AUTHENTICATION(0x18),
  /**
   * Initiate a re-authentication. Client.
   */
  RE_AUTHENTICATE(0x19);

  private static final NumberedEnumMap<AuthenticateReasonCode> NUMBERED_MAP =
      new NumberedEnumMap<>(AuthenticateReasonCode.class);

  public static AuthenticateReasonCode ofCode(int code) {
    return NUMBERED_MAP.require(code);
  }

  private final int code;

  @Override
  public int number() {
    return code;
  }
}
