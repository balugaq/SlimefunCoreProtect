package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerListener implements Listener {
    public static @NotNull String getActionString(@NotNull PlayerInteractEvent event) {
        return new ActionEntry(event.getAction()).toString();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(@NotNull EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            SlimefunItem item = SlimefunItem.getByItem(event.getItem().getItemStack());
            if (item != null) {
                Debug.debug("Insert player pickup item log");
                LogDao.insertLog(
                        player.getName(),
                        TimeUtil.now(),
                        Action.PICKUP_ITEM.getKey(),
                        LogEntry.getStringBlockLocation(player.getLocation()),
                        item.getId()
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        SlimefunItem item = SlimefunItem.getByItem(event.getItemDrop().getItemStack());
        if (item != null) {
            Debug.debug("Insert player drop item log");
            Player player = event.getPlayer();
            LogDao.insertLog(
                    player.getName(),
                    TimeUtil.now(),
                    Action.DROP_ITEM.getKey(),
                    LogEntry.getStringBlockLocation(player.getLocation()),
                    item.getId()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        SlimefunItem item = SlimefunItem.getByItem(event.getItem());
        if (item != null) {
            Debug.debug("Insert player interact item log");
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if (block == null) {
                LogDao.insertLog(
                        player.getName(),
                        TimeUtil.now(),
                        Action.INTERACT_ITEM.getKey(),
                        LogEntry.getStringLocation(player.getLocation()),
                        item.getId(),
                        getActionString(event)
                );
            } else {
                LogDao.insertLog(
                        player.getName(),
                        TimeUtil.now(),
                        Action.INTERACT_ITEM.getKey(),
                        LogEntry.getStringBlockLocation(block.getRelative(event.getBlockFace()).getLocation()),
                        item.getId(),
                        getActionString(event)
                );
            }
        }
    }

    @AllArgsConstructor
    @Data
    public static class ActionEntry {
        private final org.bukkit.event.block.@NotNull Action action;

        public static @Nullable ActionEntry fromString(@NotNull String str) {
            String[] parts = str.split(":");
            if (parts.length == 2 && parts[0].equals("iaction")) {
                try {
                    return new ActionEntry(org.bukkit.event.block.Action.valueOf(parts[1]));
                } catch (Exception e) {
                    Debug.debug("Invalid action string: " + str);
                }
            }
            return null;
        }

        public @NotNull String toString() {
            return "iaction:" + action.name();
        }
    }
}
