package com.balugaq.slimefuncoreprotect.core.managers;

import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ListenerManager {
    private final @NotNull JavaPlugin plugin;
    private final @NotNull List<Listener> listeners;

    public ListenerManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.listeners = new ArrayList<>();
    }

    public void setup() {
        // todo:
    }

    public void load() {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    public void unload() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }
}
