package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.NotHumanUsers;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockBreakEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class BlockListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(@NotNull SlimefunBlockPlaceEvent event) {
        Debug.debug("Insert block place log");
        LogDao.insertLog(
                event.getPlayer().getName(),
                TimeUtil.now(),
                Action.BLOCK_PLACE.getKey(),
                LogEntry.getStringBlockLocation(event.getBlockPlaced().getLocation()),
                event.getSlimefunItem().getId()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(@NotNull SlimefunBlockBreakEvent event) {
        Debug.debug("Insert block break log");
        LogDao.insertLog(
                event.getPlayer().getName(),
                TimeUtil.now(),
                Action.BLOCK_BREAK.getKey(),
                LogEntry.getStringBlockLocation(event.getBlockBroken().getLocation()),
                event.getSlimefunItem().getId()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlacerPlace(@NotNull BlockPlacerPlaceEvent event) {
        SlimefunItem item = SlimefunItem.getByItem(event.getItemStack());
        if (item != null) {
            Debug.debug("Insert block placer place log");
            LogDao.insertLog(
                    NotHumanUsers.USER_BLOCK_PLACER,
                    TimeUtil.now(),
                    Action.BLOCK_PLACE.getKey(),
                    LogEntry.getStringBlockLocation(event.getBlock().getLocation()),
                    item.getId()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExplosiveToolBreak(@NotNull ExplosiveToolBreakBlocksEvent event) {
        SlimefunItem item1 = StorageCacheUtils.getSfItem(event.getPrimaryBlock().getLocation());
        if (item1 != null) {
            Debug.debug("Insert explosive tool break log");
            LogDao.insertLog(
                    NotHumanUsers.USER_EXPLOSIVE_TOOL,
                    TimeUtil.now(),
                    Action.BLOCK_BREAK.getKey(),
                    LogEntry.getStringBlockLocation(event.getPrimaryBlock().getLocation()),
                    item1.getId()
            );
        }
        for (Block block : event.getAdditionalBlocks()) {
            SlimefunItem item = StorageCacheUtils.getSfItem(block.getLocation());
            if (item != null) {
                Debug.debug("Insert explosive tool break log");
                LogDao.insertLog(
                        NotHumanUsers.USER_EXPLOSIVE_TOOL,
                        TimeUtil.now(),
                        Action.BLOCK_BREAK.getKey(),
                        LogEntry.getStringBlockLocation(block.getLocation()),
                        item.getId()
                );
            }
        }
    }
}
