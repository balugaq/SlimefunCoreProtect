package com.balugaq.slimefuncoreprotect.core.commands.subcommands;

import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Lang;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.core.managers.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PageCommand extends SubCommand {
    public PageCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "page";
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
        if (args.length < 2) {
            sender.sendMessage(Lang.getMessage("invalid_page_number"));
            return false;
        }

        int page;
        try {
            page = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Lang.getMessage("invalid_page_number"));
            return false;
        }

        Map<CommandSender, Integer> pages = CommandManager.getPages();
        if (!pages.containsKey(sender)) {
            sender.sendMessage(Lang.getMessage("no_pages_found"));
            return false;
        }

        if (CommandManager.getLastLookup().containsKey(sender)) {
            List<LogEntry> entries = CommandManager.getLastLookup().get(sender);
            pages.put(sender, Math.max(1, Math.min(page, CommandManager.getMaxPage(entries))));
            CommandManager.lookup(sender, entries, null);
        }


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
