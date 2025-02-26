package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.Action;
import com.balugaq.slimefuncoreprotect.api.LogDao;
import com.balugaq.slimefuncoreprotect.api.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.Lang;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LookupCommand extends SubCommand {
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Debug.log("Lookup command executed.");
        // test
        for (LogEntry entry : LogDao.getLogsByAction(Action.BLOCK_PLACE.getKey())) {
            sender.sendMessage(entry.toString());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
