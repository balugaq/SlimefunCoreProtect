package com.balugaq.slimefuncoreprotect.api.enums;

import lombok.Getter;

@Getter
public enum Action {
    BLOCK_PLACE("block_place"),
    BLOCK_BREAK("block_break"),
    MENU_OPEN("menu_open"),
    MENU_CLOSE("menu_close"),
    ;
    private final String key;
    Action(String key) {
        this.key = key;
    }
}
