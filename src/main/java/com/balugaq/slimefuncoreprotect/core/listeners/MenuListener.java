package com.balugaq.slimefuncoreprotect.core.listeners;

import city.norain.slimefun4.holder.SlimefunInventoryHolder;
import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.BlockLogDao;
import com.balugaq.slimefuncoreprotect.api.logs.ClickLogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MenuListener implements Listener {
    private static final Set<UUID> opening = new HashSet<>();
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInit(SlimefunItemRegistryFinalizedEvent event) {
        for (BlockMenuPreset preset : Slimefun.getRegistry().getMenuPresets().values()) {
            ChestMenu.MenuOpeningHandler handler = preset.getMenuOpeningHandler();
            if (handler != null) {
                preset.addMenuOpeningHandler((p) -> {
                    opening.add(p.getUniqueId());
                    BlockLogDao.insertLog(p.getName(), TimeUtil.now(), Action.MENU_OPEN.getKey(), LogEntry.getStringBlockLocation(p.getOpenInventory().getTopInventory().getLocation()), preset.getID());
                    handler.onOpen(p);
                });
            }
            ChestMenu.MenuCloseHandler closeHandler = preset.getMenuCloseHandler();
            if (closeHandler != null) {
                preset.addMenuCloseHandler((p) -> {
                    opening.remove(p.getUniqueId());
                    BlockLogDao.insertLog(p.getName(), TimeUtil.now(), Action.MENU_CLOSE.getKey(), LogEntry.getStringBlockLocation(p.getOpenInventory().getTopInventory().getLocation()), preset.getID());
                    closeHandler.onClose(p);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (opening.contains(player.getUniqueId())) {
            if (event.getInventory() instanceof SlimefunInventoryHolder holder) {
            }
        }
    }
}
