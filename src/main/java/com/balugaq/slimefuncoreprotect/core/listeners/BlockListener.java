package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.BlockLogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockBreakEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class BlockListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(@NotNull SlimefunBlockPlaceEvent event) {
        Debug.log("Insert block place log");
        BlockLogDao.insertLog(event.getPlayer().getName(), TimeUtil.now(), Action.BLOCK_PLACE.getKey(), LogEntry.getStringBlockLocation(event.getBlockPlaced().getLocation()), event.getSlimefunItem().getId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(@NotNull SlimefunBlockBreakEvent event) {
        Debug.log("Insert block break log");
        BlockLogDao.insertLog(event.getPlayer().getName(), TimeUtil.now(), Action.BLOCK_BREAK.getKey(), LogEntry.getStringBlockLocation(event.getBlockBroken().getLocation()), event.getSlimefunItem().getId());
    }
}
