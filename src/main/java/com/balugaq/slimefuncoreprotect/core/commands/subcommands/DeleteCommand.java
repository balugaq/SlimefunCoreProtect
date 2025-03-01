package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.ConsoleQueryUser;
import com.balugaq.slimefuncoreprotect.api.QueryUser;
import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Lang;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import com.balugaq.slimefuncoreprotect.core.commands.ConsoleOnlyCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteCommand extends ConsoleOnlyCommand {
    private static final Map<ConsoleCommandSender, QueryUser> queryUsers = new HashMap<>();
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
    public boolean onCommand(@NotNull ConsoleCommandSender console, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            console.sendMessage(Lang.getMessage("commands.delete.usage"));
            console.sendMessage(Lang.getMessage("commands.delete.description"));
            return true;
        }

        String time = args[1];
        Timestamp timestamp = TimeUtil.parseTime(time);

        if (!queryUsers.containsKey(console)) {
            queryUsers.put(console, new ConsoleQueryUser(console));
        }

        List<LogEntry> logs = LogDao.getLogsBetween(queryUsers.get(console), Timestamp.valueOf("1970-01-01 00:00:00.000"), timestamp);
        if (logs.isEmpty()) {
            console.sendMessage(Lang.getMessage("commands.delete.no-logs", "time", time));
            return true;
        }

        if (args.length == 3 && "confirm".equalsIgnoreCase(args[2])) {
            for (LogEntry log : logs) {
                LogDao.deleteLog(log.getPlayer(), log.getTime(), log.getAction(), log.getPlayer(), log.getSlimefunId());
            }
            console.sendMessage(Lang.getMessage("commands.delete.success", "count", logs.size(), "time", time));
        } else {
            console.sendMessage(Lang.getMessage("commands.delete.confirm1", "count", logs.size(), "time", time));
            console.sendMessage(Lang.getMessage("commands.delete.confirm2"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull ConsoleCommandSender console, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
