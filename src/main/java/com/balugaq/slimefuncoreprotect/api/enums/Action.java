package com.balugaq.slimefuncoreprotect.api.enums;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum Action {
    BLOCK_PLACE("block_place"),
    BLOCK_BREAK("block_break"),
    MENU_OPEN("menu_open"),
    MENU_CLOSE("menu_close"),
    MENU_CLICK("inventory_click", true),
    MENU_DRAG("inventory_drag", true),
    DROP_ITEM("drop_item"),
    PICKUP_ITEM("pickup_item"),
    INTERACT_ITEM("interact_item", true),
    ;
    private final String key;
    private final boolean hasOtherData;

    Action(String key, boolean hasOtherData) {
        this.key = key;
        this.hasOtherData = hasOtherData;
    }

    Action(String key) {
        this.key = key;
        this.hasOtherData = false;
    }

    public static @Nullable Action of(String key) {
        for (Action action : Action.values()) {
            if (action.getKey().equalsIgnoreCase(key)) {
                return action;
            }

            if (action.name().equalsIgnoreCase(key)) {
                return action;
            }
        }

        return null;
    }
}
