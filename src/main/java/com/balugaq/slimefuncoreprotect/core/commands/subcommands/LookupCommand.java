package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.LogDao;
import com.balugaq.slimefuncoreprotect.api.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.core.managers.CommandManager;
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

        List<LogEntry> entries = LogDao.getLogs(commandArgs);
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
            sender.spigot().sendMessage(CommandManager.formatMessage(
                    "&b" + getDelay(entry.getTime()) +
                            "&7 - <hover><d>&b" + entry.getPlayer() + "</d><h>&b点击复制: " + entry.getPlayer() + "</h></hover>" +
                            "<click><a>copy_to_clipboard</a><v>" + entry.getPlayer() + "</v></click>" +
                            "&7 - &b" + entry.getAction() + "<hover><d>&b" + entry.getAction() + "</d><h>&b点击复制: " + entry.getAction() + "</h></hover>" +
                            "<click><a>copy_to_clipboard</a><v>" + entry.getAction() + "</v></click>" +
                            "&7 - <hover><d>&b" + LogEntry.getStringBlockLocation(location) + "</d><h>&b点击传送到该位置</h></hover>" +
                            "<click>=<a>run_command</a><v>/minecraft:tp " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + "</v></click>" +
                            "&7 - <hover><d>&b粘液 ID: " + entry.getSlimefunId() + "</d><h>&b点击复制: " + entry.getSlimefunId() + "</h></hover>" +
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
}
