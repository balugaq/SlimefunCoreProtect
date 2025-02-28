package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
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
    public void onPlayerDropItem(PlayerDropItemEvent event) {
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        SlimefunItem item = SlimefunItem.getByItem(event.getItem());
        if (item != null) {
            Debug.debug("Insert player interact item log");
            Player player = event.getPlayer();
            LogDao.insertLog(
                    player.getName(),
                    TimeUtil.now(),
                    Action.INTERACT_ITEM.getKey(),
                    LogEntry.getStringBlockLocation(player.getLocation()),
                    item.getId(),
                    getActionString(event)
            );
        }
    }

    public static String getActionString(PlayerInteractEvent event) {
        return new ActionEntry(event.getAction()).toString();
    }

    @AllArgsConstructor
    @Data
    public static class ActionEntry {
        private final org.bukkit.event.block.Action action;
        public String toString() {
            return "iaction:" + action.name();
        }

        public static ActionEntry fromString(String str) {
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
    }
}
