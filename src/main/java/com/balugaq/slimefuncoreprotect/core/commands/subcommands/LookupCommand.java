package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.BlockLogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.core.managers.CommandManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LookupCommand extends SubCommand {
    public static final Set<String> sections = new HashSet<>();
    static {
        sections.add("action:");
        sections.add("user:");
        sections.add("location:");
        sections.add("slimefunid:");
        sections.add("time:");
    }
    public LookupCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
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
        Debug.log("Lookup command executed.");
        String c = "/sco lookup ";
        for (String arg : args) {
            c += arg + " ";
        }
        c = c.trim();

        Map<String, String> commandArgs = new HashMap<>();
        for (String arg : args) {
            for (String section : sections) {
                if (arg.startsWith(section)) {
                    commandArgs.put(section, arg.replace(section, "").trim());
                }
            }
        }

        List<LogEntry> entries = BlockLogDao.getLogs(commandArgs);
        if (entries.isEmpty()) {
            sender.sendMessage("No entries found.");
            return true;
        }

        sendMessage(sender, entries, c);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    public static void lookup(@NotNull CommandSender sender, @NotNull List<LogEntry> entries, String command) {
        sendMessage(sender, entries, command);
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull List<LogEntry> entries, String command) {
        synchronized (CommandManager.getLastLookup()) {
            CommandManager.getLastLookup().put(sender, entries);
        }
        int currentPage = CommandManager.getCurrentPage(sender);
        Debug.log("Current page: " + currentPage);
        List<LogEntry> splitted = CommandManager.splitEntries(entries, currentPage);
        for (LogEntry entry : splitted) {
            Location location = entry.getLocation();
            String delay = getDelay(entry.getTime());
            String spaces = " ".repeat(delay.length() + 3);
            sender.spigot().sendMessage(CommandManager.formatMessage(
                    "&e" + delay +
                            "&7 * <hover><d>&b" + entry.getPlayer() + "</d><h>&d点击复制: " + entry.getPlayer() + "</h></hover>" +
                            "<click><a>copy_to_clipboard</a><v>" + entry.getPlayer() + "</v></click>" +
                            "&7 * <hover><d>&f" + humanizeAction(entry.getAction()) + "</d><h>&d点击复制: " + humanizeAction(entry.getAction()) + "</h></hover>" +
                            "<click><a>copy_to_clipboard</a><v>" + humanizeAction(entry.getAction()) + "</v></click>"
            ));

            newline();
            sender.spigot().sendMessage(CommandManager.formatMessage(
                            "&7 * <hover><d>" + spaces + "&7&o(x" + location.getBlockX() + "/y" + location.getBlockY() + "/z" + location.getBlockZ() + "/" + location.getWorld().getName() + ")" + "</d><h>&b点击传送到该位置</h></hover>" +
                            "<click>=<a>run_command</a><v>/minecraft:tp " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + "</v></click>"
            ));

            newline();
            sender.spigot().sendMessage(CommandManager.formatMessage(
                            "&7 * <hover><d>" + spaces + "&a&o物品: " + getName(entry.getSlimefunId()) + "</d><h>&d点击复制: " + entry.getSlimefunId() + "</h></hover>" +
                            "<click><a>copy_to_clipboard</a><v>" + entry.getSlimefunId() + "</v></click>"
            ));
        }
        CommandManager.pageSwitch(sender, entries, command);
    }

    public static String getDelay(@NotNull Timestamp timestamp) {
        double delay = (System.currentTimeMillis() - timestamp.getTime()) / 1000.0D; // in seconds
        // if less than 1 minute, show seconds
        if (delay < 60.0D) {
            return String.format("%.2fs", delay);
        }
        // if less than 1 hour, show minutes with dot
        if (delay < 3600.0D) {
            return String.format("%.2fm", delay / 60.0D);
        }
        // if less than 1 day, show hours with dot
        if (delay < 86400.0D) {
            return String.format("%.2fh", delay / 3600.0D);
        }
        // if more than 1 day, show days with dot
        return String.format("%.2fd", delay / 86400.0D);
    }

    // the opposite of getDelay()
    public static Timestamp parseTime(String timeStr) {
        if (timeStr.endsWith("s")) {
            double seconds = Double.parseDouble(timeStr.replace("s", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (seconds * 1000));
        } else if (timeStr.endsWith("m")) {
            double minutes = Double.parseDouble(timeStr.replace("m", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (minutes * 60 * 1000));
        } else if (timeStr.endsWith("h")) {
            double hours = Double.parseDouble(timeStr.replace("h", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (hours * 3600 * 1000));
        } else if (timeStr.endsWith("d")) {
            double days = Double.parseDouble(timeStr.replace("d", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (days * 86400 * 1000));
        } else {
            return null;
        }
    }

    // A empty method, just notice the reader
    public static void newline() {
    }

    public static String humanizeAction(String actionStr) {
        Action action = Action.valueOf(actionStr.toUpperCase());
        return switch (action) {
            case BLOCK_PLACE -> "&a&l+block&r";
            case BLOCK_BREAK -> "&c&l-block&r";
            case MENU_OPEN -> "&a&l+menu&r";
            case MENU_CLOSE -> "&c&l-menu&r";
            default -> "&7" + action.toString().toLowerCase();
        };
    }

    public static Action unhumanizeAction(String actionStr) {
        return switch (actionStr.toLowerCase()) {
            case "+block" -> Action.BLOCK_PLACE;
            case "-block" -> Action.BLOCK_BREAK;
            case "+menu" -> Action.MENU_OPEN;
            case "-menu" -> Action.MENU_CLOSE;
            default -> Action.valueOf(actionStr.toUpperCase());
        };
    }

    public static String getName(String slimefunId) {
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);
        if (slimefunItem == null) {
            return slimefunId;
        }

        return slimefunItem.getItemName();
    }
}
