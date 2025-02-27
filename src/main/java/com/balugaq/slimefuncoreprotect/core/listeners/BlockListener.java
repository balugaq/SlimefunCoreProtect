package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.Action;
import com.balugaq.slimefuncoreprotect.api.LogDao;
import com.balugaq.slimefuncoreprotect.api.LogEntry;
import com.balugaq.slimefuncoreprotect.api.TimeUtil;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockBreakEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Instant;

public class BlockListener implements Listener {
    @EventHandler
    public void onBlockPlace(@NotNull SlimefunBlockPlaceEvent event) {
        Debug.log("Insert block place log");
        LogDao.insertLog(event.getPlayer().getName(), TimeUtil.now(), Action.BLOCK_PLACE.getKey(), LogEntry.getStringBlockLocation(event.getBlockPlaced().getLocation()), event.getSlimefunItem().getId());
    }

    @EventHandler
    public void onBlockBreak(@NotNull SlimefunBlockBreakEvent event) {
        Debug.log("Insert block break log");
        LogDao.insertLog(event.getPlayer().getName(), TimeUtil.now(), Action.BLOCK_BREAK.getKey(), LogEntry.getStringBlockLocation(event.getBlockBroken().getLocation()), event.getSlimefunItem().getId());
    }
}
