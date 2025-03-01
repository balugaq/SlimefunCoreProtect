package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.PlayerQueryUser;
import com.balugaq.slimefuncoreprotect.api.QueryUser;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.core.managers.CommandManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InspectListener implements Listener {
    private static final Map<Player, QueryUser> queryUsers = new HashMap<>();
    private static final Set<UUID> inspectingPlayers = new HashSet<>();

    public static boolean isInspecting(UUID player) {
        return inspectingPlayers.contains(player);
    }

    public static void addInspectingPlayer(UUID player) {
        inspectingPlayers.add(player);
    }

    public static void removeInspectingPlayer(UUID player) {
        inspectingPlayers.remove(player);
    }

    public static @NotNull List<LogEntry> getLogs(Player player, Location location) {
        if (!queryUsers.containsKey(player)) {
            queryUsers.put(player, new PlayerQueryUser(player));
        }

        return LogDao.getLogsByLocation(queryUsers.get(player), LogEntry.getStringBlockLocation(location));
    }

    public static void inspect(@NotNull Player player, Location location) {
        List<LogEntry> logs = getLogs(player, location);
        CommandManager.lookup(player, logs, null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInspect(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isInspecting(player.getUniqueId())) {
            inspect(player, event.getBlock().getLocation());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInspect(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (isInspecting(player.getUniqueId())) {
                inspect(player, event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
                event.setCancelled(true);
            }
        }
    }
}
