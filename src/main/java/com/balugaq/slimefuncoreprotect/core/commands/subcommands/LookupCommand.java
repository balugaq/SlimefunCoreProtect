package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.QueryUser;
import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.Lang;
import com.balugaq.slimefuncoreprotect.api.utils.NotHumanUsers;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.core.listeners.MenuListener;
import com.balugaq.slimefuncoreprotect.core.listeners.PlayerListener;
import com.balugaq.slimefuncoreprotect.core.managers.CommandManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LookupCommand extends SubCommand {
    private static final Set<String> SLIMEFUN_ITEM_IDS = new HashSet<>();
    private static final Set<String> SECTIONS = new HashSet<>();
    private static final Map<String, List<Action>> UNCOMMON_SECTIONS = new HashMap<>();
    private static final Map<CommandSender, QueryUser> queryUsers = new HashMap<>();

    static {
        SECTIONS.add("action:");
        SECTIONS.add("user:");
        SECTIONS.add("location:");
        SECTIONS.add("slimefunid:");
        SECTIONS.add("time:");
        UNCOMMON_SECTIONS.put("cursor:", List.of(Action.MENU_CLICK, Action.MENU_DRAG));
        UNCOMMON_SECTIONS.put("clicked:", List.of(Action.MENU_CLICK));
        UNCOMMON_SECTIONS.put("radius:", List.of(Action.values()));
        UNCOMMON_SECTIONS.put("iaction:", List.of(Action.INTERACT_ITEM));

        for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            SLIMEFUN_ITEM_IDS.add(item.getId());
        }
    }

    public LookupCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    public static Set<String> getSlimefunItemIds() {
        return SLIMEFUN_ITEM_IDS;
    }

    public static Set<String> getSections() {
        return SECTIONS;
    }

    public static Map<String, List<Action>> getUncommonSections() {
        return UNCOMMON_SECTIONS;
    }

    @Nullable
    public static Action getAction(String @NotNull [] args) {
        for (String arg : args) {
            if (arg.equals("action:")) {
                return Action.valueOf(arg.replace("action:", "").trim());
            }
        }

        return null;
    }

    public static @NotNull Map<String, String> getCommandArgs(String @NotNull [] args) {
        Map<String, String> commandArgs = new HashMap<>();
        for (String arg : args) {
            for (String section : SECTIONS) {
                if (arg.startsWith(section)) {
                    String value = arg.replace(section, "").trim();
                    if (section.equals("action:")) {
                        Action action = CommandManager.unhumanizeAction(value);
                        commandArgs.put(section, action.getKey());
                    } else {
                        commandArgs.put(section, value);
                    }
                }
            }
        }

        return commandArgs;
    }

    public static boolean handleOtherLookupArgs(String @NotNull [] args, @Nullable Action action, @NotNull CommandSender sender, @NotNull List<LogEntry> entries, @NotNull List<LogEntry> removes) {
        for (String arg : args) {
            for (String section : UNCOMMON_SECTIONS.keySet()) {
                if (arg.startsWith(section)) {
                    List<Action> allowedActions = UNCOMMON_SECTIONS.get(section);
                    if (allowedActions != null) {
                        if (allowedActions.size() != Action.values().length) {
                            if (action == null) {
                                sender.sendMessage(Lang.getMessage("commands.lookup.no-action-specified", "section", section));
                                return false;
                            } else {
                                if (!allowedActions.contains(action)) {
                                    sender.sendMessage(Lang.getMessage("commands.lookup.not-allowed-action", "section", section, "action", CommandManager.humanizeAction(action)));
                                    return false;
                                }
                            }
                        }
                    }

                    switch (section) {
                        case "cursor:" -> {
                            String value = arg.replace(section, "").trim();
                            for (LogEntry entry : entries) {
                                if (entry.getOtherData() == null) {
                                    removes.add(entry);
                                    continue;
                                }

                                MenuListener.ClickEntry clickEntry = MenuListener.ClickEntry.fromString(entry.getOtherData());
                                if (clickEntry == null) {
                                    MenuListener.DragEntry dragEntry = MenuListener.DragEntry.fromString(entry.getOtherData());
                                    if (dragEntry == null) {
                                        removes.add(entry);
                                        continue;
                                    }

                                    if (!dragEntry.getCursor().contains(value)) {
                                        removes.add(entry);
                                        continue;
                                    }
                                    continue;
                                } else {
                                    if (!clickEntry.getCursor().contains(value)) {
                                        removes.add(entry);
                                        continue;
                                    }
                                }
                            }
                        }
                        case "clicked:" -> {
                            String value = arg.replace(section, "").trim();
                            for (LogEntry entry : entries) {
                                if (entry.getOtherData() == null) {
                                    removes.add(entry);
                                    continue;
                                }

                                MenuListener.ClickEntry clickEntry = MenuListener.ClickEntry.fromString(entry.getOtherData());
                                if (clickEntry == null) {
                                    removes.add(entry);
                                    continue;
                                }

                                if (!clickEntry.getClicked().contains(value)) {
                                    removes.add(entry);
                                    continue;
                                }
                            }
                        }
                        case "iaction:" -> {
                            String value = arg.replace(section, "").trim();
                            for (LogEntry entry : entries) {
                                if (entry.getOtherData() == null) {
                                    removes.add(entry);
                                    continue;
                                }

                                PlayerListener.ActionEntry actionEntry = PlayerListener.ActionEntry.fromString(entry.getOtherData());
                                if (actionEntry == null) {
                                    removes.add(entry);
                                    continue;
                                }

                                if (!actionEntry.getAction().name().contains(value)) {
                                    removes.add(entry);
                                    continue;
                                }
                            }
                        }
                        case "radius:" -> {
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Lang.getMessage("commands.lookup.not-player-for-radius-section"));
                                return false;
                            }
                            Location center = player.getLocation();
                            World world = center.getWorld();
                            String value = arg.replace(section, "").trim();
                            double radius = Double.parseDouble(value);
                            for (LogEntry entry : entries) {
                                Location location = entry.getLocation();
                                if (location == null) {
                                    removes.add(entry);
                                    continue;
                                }

                                if (world != location.getWorld()) {
                                    removes.add(entry);
                                    continue;
                                }

                                if (location.distance(center) > radius) {
                                    removes.add(entry);
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public @NotNull String getName() {
        return "lookup";
    }

    @Override
    public boolean canCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        return getName().equalsIgnoreCase(args[0]);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Debug.debug("Lookup command executed.");
        StringBuilder c = new StringBuilder("/sco lookup ");
        for (String arg : args) {
            c.append(arg).append(" ");
        }
        c = new StringBuilder(c.toString().trim());

        Action action = getAction(args);
        Map<String, String> commandArgs = getCommandArgs(args);
        for (Map.Entry<String, String> entry : commandArgs.entrySet()) {
            Debug.debug("Command arg: " + entry.getKey() + " = " + entry.getValue());
        }

        if (!queryUsers.containsKey(sender)) {
            queryUsers.put(sender, QueryUser.create(sender));
        }
        List<LogEntry> entries = LogDao.getLogs(queryUsers.get(sender), commandArgs);
        if (entries.isEmpty()) {
            sender.sendMessage(Lang.getMessage("commands.lookup.no-logs-found"));
            return true;
        }

        if (args.length == 1) {
            args = new String[]{"radius:10"};
        }

        List<LogEntry> removes = new ArrayList<>();
        if (!handleOtherLookupArgs(args, action, sender, entries, removes)) {
            return false;
        }

        entries.removeAll(removes);

        CommandManager.lookup(sender, entries, c.toString());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Set<String> phase1 = new HashSet<>();
        Set<String> contains = new HashSet<>();
        for (String arg : args) {
            if (arg.contains(":")) {
                String[] parts = arg.split(":");
                if (parts.length == 2) {
                    String section = parts[0].trim();
                    contains.add(section);
                }
            }
        }

        for (String section : SECTIONS) {
            if (contains.contains(section.split(":")[0].trim())) {
                continue;
            }
            phase1.add(section);
        }

        for (String section : UNCOMMON_SECTIONS.keySet()) {
            if (contains.contains(section.split(":")[0].trim())) {
                continue;
            }
            phase1.add(section);
        }

        Set<String> phase2 = new HashSet<>();
        String last = args[args.length - 1];
        if (last.startsWith("action:")) {
            for (Action action : Action.values()) {
                String s = "action:" + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', CommandManager.humanizeAction(action)));
                phase2.add(s);
            }
        }

        if (last.startsWith("user:")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String s = "user:" + player.getName();
                phase2.add(s);
            }

            for (String string : NotHumanUsers.getNotHumanUsers()) {
                String s = "user:" + string;
                phase2.add(s);
            }
        }

        if (last.startsWith("location:")) {
            if (sender instanceof Player player) {
                Block block = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
                Location location = null;
                if (block != null) {
                    location = block.getLocation();
                } else {
                    location = player.getLocation();
                }

                String s = "location:" + LogEntry.getStringBlockLocation(location);
                phase2.add(s);
            }
        }

        if (last.startsWith("slimefunid:")) {
            for (String id : SLIMEFUN_ITEM_IDS) {
                String s = "slimefunid:" + id;
                phase2.add(s);
            }
        }

        List<String> times = List.of("1m", "10m", "30m", "1h", "12h", "1d", "30d");
        if (last.startsWith("time:")) {
            for (String time : times) {
                String s = "time:" + time;
                phase2.add(s);
            }
        }

        List<String> rawResult = new ArrayList<>();
        rawResult.addAll(phase2);
        rawResult.addAll(phase1);

        List<String> removed = new ArrayList<>();
        for (String arg : args) {
            if (rawResult.contains(arg)) {
                removed.add(arg);
            }
        }

        List<String> result = new ArrayList<>(rawResult);
        result.removeAll(removed);

        return result;
    }
}
