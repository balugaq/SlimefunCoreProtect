package com.balugaq.slimefuncoreprotect.api.utils;

import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import org.jetbrains.annotations.NotNull;

public class Lang {
    public static @NotNull String getMessage(String path) {
        return SlimefunCoreProtect.getInstance().getLocalizationService().getString("messages." + path);
    }

    public static @NotNull String getMessage(String path, Object @NotNull ... args) {
        String message = getMessage(path);
        for (int i = 0; i < args.length; i += 2) {
            message = message.replace("{" + args[i] + "}", args[i + 1].toString());
        }
        return message;
    }
}
