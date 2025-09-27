package javasabr.mqtt.legacy.model;

public enum ActionResult {
  SUCCESS,
  FAILED,
  EMPTY;

  public ActionResult and(ActionResult another) {
    if (this == FAILED || another == FAILED) {
      return FAILED;
    } else if (this == SUCCESS || another == SUCCESS) {
      return SUCCESS;
    } else {
      return EMPTY;
    }
  }
}
