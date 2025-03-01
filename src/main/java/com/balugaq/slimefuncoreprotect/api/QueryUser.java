package com.balugaq.slimefuncoreprotect.api;

import lombok.Getter;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

@Getter
public abstract class QueryUser {
    private static final Map<QueryUser, Long> lastQueryTime = new WeakHashMap<>();
    public abstract @NotNull String getUsername();
    public abstract boolean isOnline();
    public abstract boolean isOp();
    public abstract boolean hasPermission(String node);
    public abstract void sendMessage(String message);
    public static void updateLastQueryTime(QueryUser user) {
        lastQueryTime.put(user, System.currentTimeMillis());
    }

    public static long getLastQueryTime(QueryUser user) {
        return lastQueryTime.getOrDefault(user, 0L);
    }

    public static void clearLastQueryTime(QueryUser user) {
        lastQueryTime.remove(user);
    }

    public static void clearAllLastQueryTime() {
        lastQueryTime.clear();
    }

    public static boolean isOutdated(QueryUser user, long timeout) {
        return System.currentTimeMillis() - getLastQueryTime(user) > timeout;
    }

    @Contract("null -> null; !null -> new")
    public static QueryUser create(ConsoleCommandSender console) {
        if (console == null) {
            return null;
        }

        return new ConsoleQueryUser(console);
    }

    @Contract("null -> null; !null -> new")
    public static QueryUser create(Player player) {
        if (player == null) {
            return null;
        }

        return new PlayerQueryUser(player);
    }

    @Nullable
    public static QueryUser create(Object object) {
        if (object instanceof ConsoleCommandSender console) {
            return create(console);
        }

        else if (object instanceof Player player) {
            return create(player);
        }

        return null;
    }
}
