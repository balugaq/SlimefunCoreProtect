package com.balugaq.slimefuncoreprotect.api;

import lombok.Getter;

@Getter
public enum Action {
    BLOCK_PLACE("block_place"),
    BLOCK_BREAK("block_break"),
    ;
    private final String key;
    Action(String key) {
        this.key = key;
    }
}
