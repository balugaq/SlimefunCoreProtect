package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import com.balugaq.slimefuncoreprotect.core.commands.ConsoleOnlyCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;

public class DeleteCommand extends ConsoleOnlyCommand {
    public DeleteCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "delete";
    }

    @Override
    public boolean canCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        return getName().equalsIgnoreCase(args[0]);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull ConsoleCommandSender console, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            console.sendMessage("Usage: /slimefun delete time:<time>");
            console.sendMessage("Delete all logs older than <time>");
            return true;
        }

        String time = args[1];
        Timestamp timestamp = TimeUtil.parseTime(time);
        List<LogEntry> logs = LogDao.getLogsBetween(timestamp, Timestamp.valueOf("1970-01-01 00:00:00.000"));
        if (logs.isEmpty()) {
            console.sendMessage("No logs found before time: " + time);
            return true;
        }

        if (args.length == 3 && "confirm".equalsIgnoreCase(args[2])) {
            for (LogEntry log : logs) {
                LogDao.deleteLog(log.getPlayer(), log.getTime(), log.getAction(), log.getPlayer(), log.getSlimefunId());
            }
            console.sendMessage("Deleted " + logs.size() + " logs older than " + time);
        } else {
            console.sendMessage("Are you sure you want to delete " + logs.size() + " logs older than " + time + "? (It cannot be undone!!!)");
            console.sendMessage("Type /slimefun delete time:" + time + " confirm to confirm deletion.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull ConsoleCommandSender console, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
