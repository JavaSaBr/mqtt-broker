package com.ss.mqtt.broker.model;

import org.jetbrains.annotations.NotNull;

public enum ActionResult {

    SUCCESS,
    FAILED,
    EMPTY;

    public @NotNull ActionResult and(@NotNull ActionResult another) {
        if (this == FAILED || another == FAILED) {
            return FAILED;
        } else if (this == SUCCESS || another == SUCCESS) {
            return SUCCESS;
        } else {
            return EMPTY;
        }
    }
}
