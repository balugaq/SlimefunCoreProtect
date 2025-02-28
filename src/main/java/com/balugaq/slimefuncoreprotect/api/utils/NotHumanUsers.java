package com.balugaq.slimefuncoreprotect.api.utils;

import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class NotHumanUsers {
    public static final Map<String, Plugin> NOT_HUMAN_USERS = new HashMap<>();
    public static final String USER_EXPLOSIVE_TOOL = "#ExplosiveTool";
    public static final String USER_BLOCK_PLACER = "#BlockPlacer";
    static {
        NOT_HUMAN_USERS.put(USER_EXPLOSIVE_TOOL, SlimefunCoreProtect.getInstance());
        NOT_HUMAN_USERS.put(USER_BLOCK_PLACER, SlimefunCoreProtect.getInstance());
    }

    public static Set<String> getNotHumanUsers() {
        return NOT_HUMAN_USERS.keySet();
    }
}
