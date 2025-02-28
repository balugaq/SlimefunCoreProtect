package com.balugaq.slimefuncoreprotect.core.listeners;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MenuListener implements Listener {
    public static final String UNDEFINED = "undefined";
    private static final Set<UUID> opening = new HashSet<>();

    public static String getDragString(@NotNull InventoryDragEvent event) {
        return new DragEntry(event.getType(), event.getRawSlots(), ItemUtils.getItemName(event.getWhoClicked().getItemOnCursor())).toString();
    }

    public static @NotNull String getSlotsString(@NotNull Set<Integer> slots) {
        if (slots.isEmpty()) {
            return UNDEFINED;
        }
        StringBuilder str = new StringBuilder();
        for (int slot : slots.stream().sorted().toList()) {
            str.append(slot).append(",");
        }
        if (str.charAt(str.length() - 1) == ',') {
            str = new StringBuilder(str.substring(0, str.length() - 1));
        }
        return str.toString();
    }

    public static String getClickString(@NotNull InventoryClickEvent event) {
        return new ClickEntry(event.getClick(), event.getAction(), event.getRawSlot(), ItemUtils.getItemName(event.getWhoClicked().getItemOnCursor()), ItemUtils.getItemName(event.getWhoClicked().getItemOnCursor())).toString();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInit(SlimefunItemRegistryFinalizedEvent event) {
        for (BlockMenuPreset preset : Slimefun.getRegistry().getMenuPresets().values()) {
            ChestMenu.MenuOpeningHandler handler = preset.getMenuOpeningHandler();
            if (handler != null) {
                preset.addMenuOpeningHandler((p) -> {
                    Debug.debug("Insert menu open log");
                    opening.add(p.getUniqueId());
                    LogDao.insertLog(p.getName(), TimeUtil.now(), Action.MENU_OPEN.getKey(), LogEntry.getStringBlockLocation(p.getOpenInventory().getTopInventory().getLocation()), preset.getID());
                    handler.onOpen(p);
                });
            }
            ChestMenu.MenuCloseHandler closeHandler = preset.getMenuCloseHandler();
            if (closeHandler != null) {
                preset.addMenuCloseHandler((p) -> {
                    Debug.debug("Insert menu close log");
                    opening.remove(p.getUniqueId());
                    LogDao.insertLog(p.getName(), TimeUtil.now(), Action.MENU_CLOSE.getKey(), LogEntry.getStringBlockLocation(p.getOpenInventory().getTopInventory().getLocation()), preset.getID());
                    closeHandler.onClose(p);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getHolder() instanceof BlockMenu menu) {
            Debug.debug("Insert menu click log");
            LogDao.insertLog(
                    player.getName(),
                    TimeUtil.now(),
                    Action.MENU_CLICK.getKey(),
                    LogEntry.getStringBlockLocation(event.getInventory().getLocation()),
                    menu.getPreset().getID(),
                    getClickString(event)
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuDrag(@NotNull InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getHolder() instanceof BlockMenu menu) {
            Debug.debug("Insert menu drag log");
            LogDao.insertLog(
                    player.getName(),
                    TimeUtil.now(),
                    Action.MENU_DRAG.getKey(),
                    LogEntry.getStringBlockLocation(event.getInventory().getLocation()),
                    menu.getPreset().getID(),
                    getDragString(event)
            );
        }
    }

    @AllArgsConstructor
    @Data
    public static class ClickEntry {
        private final @NotNull ClickType clickType;
        private final @NotNull InventoryAction action;
        private final int slot;
        private final @NotNull String cursor;
        private final @NotNull String clicked;

        @Nullable
        public static ClickEntry fromString(@NotNull String str) {
            String[] parts = str.split(";");
            if (parts.length != 5) {
                return null;
            }
            ClickType clickType = ClickType.valueOf(parts[0]);
            InventoryAction action = InventoryAction.valueOf(parts[1]);
            int slot;
            try {
                slot = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return null;
            }
            String cursor = parts[3];
            String clicked = parts[4];
            return new ClickEntry(clickType, action, slot, cursor, clicked);
        }

        public @NotNull String toString() {
            return clickType.name() + ";" + action.name() + ";" + slot + ";" + cursor + ";" + clicked;
        }
    }

    @AllArgsConstructor
    @Data
    public static class DragEntry {
        private final @NotNull DragType dragType;
        private final @NotNull Set<Integer> slots;
        private final @NotNull String cursor;

        @Nullable
        public static DragEntry fromString(@NotNull String str) {
            String[] parts = str.split(";");
            if (parts.length != 3) {
                return null;
            }
            DragType dragType = DragType.valueOf(parts[0]);
            Set<Integer> slots = new HashSet<>();
            String[] slotParts = parts[1].split(",");
            for (String slot : slotParts) {
                if (slot.equals(UNDEFINED)) {
                    return null;
                }
                try {
                    slots.add(Integer.parseInt(slot));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            String itemName = parts[2];
            return new DragEntry(dragType, slots, itemName);
        }

        public @NotNull String toString() {
            return dragType.name() + ";" + getSlotsString(slots) + ";" + cursor;
        }
    }
}
