package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.logs.LogDao;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Lang;
import com.balugaq.slimefuncoreprotect.core.commands.PlayerOnlyCommand;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.core.listeners.InspectListener;
import com.balugaq.slimefuncoreprotect.core.managers.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InspectCommand extends PlayerOnlyCommand {
    public InspectCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "inspect";
    }

    @Override
    public boolean canCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        return getName().equalsIgnoreCase(args[0]);
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        UUID uuid = player.getUniqueId();
        if (InspectListener.isInspecting(uuid)) {
            InspectListener.removeInspectingPlayer(uuid);
            player.sendMessage(Lang.getMessage("commands.inspect.stop"));
        } else {
            InspectListener.addInspectingPlayer(uuid);
            player.sendMessage(Lang.getMessage("commands.inspect.start"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
