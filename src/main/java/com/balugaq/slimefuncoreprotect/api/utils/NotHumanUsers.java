package com.balugaq.slimefuncoreprotect.api.utils;

import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class NotHumanUsers {
    private static final Map<String, Plugin> NOT_HUMAN_USERS = new HashMap<>();
    private static final String USER_EXPLOSIVE_TOOL = "#ExplosiveTool";
    private static final String USER_BLOCK_PLACER = "#BlockPlacer";

    static {
        NOT_HUMAN_USERS.put(USER_EXPLOSIVE_TOOL, SlimefunCoreProtect.getInstance());
        NOT_HUMAN_USERS.put(USER_BLOCK_PLACER, SlimefunCoreProtect.getInstance());
    }

    public static @NotNull Set<String> getNotHumanUsers() {
        return NOT_HUMAN_USERS.keySet();
    }

    public static @NotNull String getUserExplosiveTool() {
        return USER_EXPLOSIVE_TOOL;
    }

    public static @NotNull String getUserBlockPlacer() {
        return USER_BLOCK_PLACER;
    }
}
