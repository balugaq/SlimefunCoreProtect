package com.balugaq.slimefuncoreprotect.core.managers;

import com.balugaq.slimefuncoreprotect.api.enums.Action;
import com.balugaq.slimefuncoreprotect.api.logs.LogEntry;
import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import com.balugaq.slimefuncoreprotect.api.utils.Lang;
import com.balugaq.slimefuncoreprotect.api.utils.TimeUtil;
import com.balugaq.slimefuncoreprotect.core.commands.SubCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.DeleteCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.HelpCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.InspectCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.LookupCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.PageCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.ReloadCommand;
import com.balugaq.slimefuncoreprotect.core.commands.subcommands.VersionCommand;
import com.balugaq.slimefuncoreprotect.core.listeners.MenuListener;
import com.balugaq.slimefuncoreprotect.implementation.SlimefunCoreProtect;
import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class CommandManager implements TabExecutor {
    private static final String EMPTY_PLACEHOLDER = "<hover><d></d><h></h></hover>";
    private static final Pattern CLICK_TAG_PATTERN = Pattern.compile("<a>(.*?)</a><v>(.*?)</v>", Pattern.DOTALL);
    private static final Pattern HOVER_TAG_PATTERN = Pattern.compile("<d>(.*?)</d><h>(.*?)</h>", Pattern.DOTALL);
    @Getter
    private static final Map<CommandSender, List<LogEntry>> lastLookup = new HashMap<>();
    @Getter
    private static final Map<CommandSender, Integer> pages = new HashMap<>();
    private static final String ROOT_COMMAND = "slimefuncoreprotect";
    private static final int MAX_CONTENT_PER_PAGE = SlimefunCoreProtect.getInstance().getConfigManager().getMaxContentPerPage();
    private static final Pattern TAG_PATTERN = Pattern.compile("(.*?)(<(hover|click)>(.*?)</\\3>)", Pattern.DOTALL);
    private final @NotNull JavaPlugin plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final @NotNull SubCommand defaultCommand;
    public CommandManager(@NotNull JavaPlugin plugin) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null");
        this.plugin = plugin;
        this.defaultCommand = new HelpCommand(plugin);
        setup();
        registerCommand();
    }

    public static String getEmptyPlaceholder() {
        return EMPTY_PLACEHOLDER;
    }

    public static Pattern getClickTagPattern() {
        return CLICK_TAG_PATTERN;
    }

    public static Pattern getHoverTagPattern() {
        return HOVER_TAG_PATTERN;
    }

    public static @NotNull String humanizeAction(@NotNull String actionStr) {
        return humanizeAction(Action.of(actionStr));
    }

    public static @NotNull String humanizeAction(@Nullable Action action) {
        if (action == null) {
            return "unknown";
        }

        return switch (action) {
            case BLOCK_PLACE -> "&a&l+block&r";
            case BLOCK_BREAK -> "&c&l-block&r";
            case MENU_OPEN -> "&a&l+menu&r";
            case MENU_CLOSE -> "&c&l-menu&r";
            case MENU_CLICK -> "&f&l?click&r";
            case DROP_ITEM -> "&c&l-item&r";
            case PICKUP_ITEM -> "&a&l+item&r";
            case INTERACT_ITEM -> "&f&l?use&r";
            case MENU_DRAG -> "&f&l?drag&r";
        };
    }

    public static @NotNull Action unhumanizeAction(@NotNull String actionStr) {
        return switch (actionStr.toLowerCase()) {
            case "+block" -> Action.BLOCK_PLACE;
            case "-block" -> Action.BLOCK_BREAK;
            case "+menu" -> Action.MENU_OPEN;
            case "-menu" -> Action.MENU_CLOSE;
            case "?click" -> Action.MENU_CLICK;
            case "-item" -> Action.DROP_ITEM;
            case "+item" -> Action.PICKUP_ITEM;
            case "?use" -> Action.INTERACT_ITEM;
            case "?drag" -> Action.MENU_DRAG;
            default -> Action.valueOf(actionStr.toUpperCase());
        };
    }

    public static void lookup(@NotNull CommandSender sender, @NotNull List<LogEntry> entries, String command) {
        synchronized (getLastLookup()) {
            getLastLookup().put(sender, entries);
        }
        int currentPage = getCurrentPage(sender);
        Debug.debug("Current page: " + currentPage);
        List<LogEntry> splitted = splitEntries(entries, currentPage);
        for (LogEntry entry : splitted) {
            Location location = entry.getLocation();
            String delay = TimeUtil.getDelay(entry.getTime());
            String spaces = " ".repeat(delay.length() + 3);
            sender.spigot().sendMessage(formatMessage(Lang.getMessage("commands.lookups.common",
                    "delay", delay,
                    "player", entry.getPlayer(),
                    "action", humanizeAction(entry.getAction())
            )));

            Action action = Action.of(entry.getAction());
            if (action == Action.MENU_CLICK) {
                String otherData = entry.getOtherData();
                MenuListener.ClickEntry clickEntry = MenuListener.ClickEntry.fromString(otherData);
                if (clickEntry != null) {
                    newline();
                    sender.spigot().sendMessage(formatMessage(Lang.getMessage("commands.lookups.menu-click1",
                            "spaces", spaces,
                            "cursor", clickEntry.getCursor(),
                            "clicked", clickEntry.getClicked(),
                            "slot", clickEntry.getSlot(),
                            "click_type", clickEntry.getClickType().name()
                    )));

                    newline();
                    sender.spigot().sendMessage(formatMessage(Lang.getMessage("commands.lookups.menu-click2",
                            "spaces", spaces,
                            "cursor", clickEntry.getCursor(),
                            "clicked", clickEntry.getClicked(),
                            "slot", clickEntry.getSlot(),
                            "click_type", clickEntry.getClickType().name()
                    )));
                }
            } else if (action == Action.MENU_DRAG) {
                String otherData = entry.getOtherData();
                MenuListener.DragEntry dragEntry = MenuListener.DragEntry.fromString(otherData);
                if (dragEntry != null) {
                    List<Integer> slots = dragEntry.getSlots().stream().sorted().toList();
                    newline();
                    sender.spigot().sendMessage(formatMessage(Lang.getMessage("commands.lookups.menu-drag",
                            "spaces", spaces,
                            "cursor", dragEntry.getCursor(),
                            "slots", slots.toString(),
                            "drag_type", dragEntry.getDragType().name()
                    )));
                }
            }

            if (location != null) {
                newline();
                sender.spigot().sendMessage(formatMessage(Lang.getMessage("commands.lookups.location",
                        "spaces", spaces,
                        "x", location.getBlockX(),
                        "y", location.getBlockY(),
                        "z", location.getBlockZ(),
                        "world", location.getWorld().getName()
                )));
            }

            newline();
            sender.spigot().sendMessage(formatMessage(Lang.getMessage("commands.lookups.slimefun-id",
                    "spaces", spaces,
                    "name", getName(entry.getSlimefunId()),
                    "id", entry.getSlimefunId()
            )));
        }

        if (!splitted.isEmpty()) {
            sendPageSwitch(sender, entries, command);
        } else {
            sender.sendMessage(Lang.getMessage("commands.lookup.no-logs-found"));
        }
    }

    // An empty method, just notice the reader
    public static void newline() {
    }

    public static @NotNull String getName(@NotNull String slimefunId) {
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);
        if (slimefunItem == null) {
            return slimefunId;
        }

        return slimefunItem.getItemName();
    }

    public static @NotNull List<LogEntry> splitEntries(@NotNull List<LogEntry> entries, int page) {
        if (entries.isEmpty()) {
            return entries;
        }

        int end = page * MAX_CONTENT_PER_PAGE;
        if (end > entries.size()) {
            end = entries.size();
        }

        int start = Math.max(0, end - MAX_CONTENT_PER_PAGE);

        return entries.subList(start, end);
    }

    public static int getCurrentPage(CommandSender sender) {
        return pages.computeIfAbsent(sender, k -> 1);
    }

    public static int getMaxPage(@NotNull List<LogEntry> entries) {
        int size = entries.size();
        if (size == 0) {
            return 1;
        }
        return (size + MAX_CONTENT_PER_PAGE - 1) / MAX_CONTENT_PER_PAGE;
    }

    public static BaseComponent @NotNull [] formatMessage(@NotNull String message) {
        ComponentBuilder builder = new ComponentBuilder();
        try {
            while (!message.isEmpty()) {
                Matcher matcher = TAG_PATTERN.matcher(message);
                if (!matcher.find()) {
                    builder.append(format(message));
                    break;
                }

                String normalText = matcher.group(1);
                if (!normalText.isEmpty()) {
                    builder.append(format(normalText));
                }

                // Unused
                // String fullTag = matcher.group(2);
                String tagType = matcher.group(3);
                String tagContent = matcher.group(4);

                if ("hover".equals(tagType)) {
                    processHoverTag(builder, tagContent);
                } else if ("click".equals(tagType)) {
                    processClickTag(builder, tagContent);
                }

                message = message.substring(matcher.end(2));
            }
        } catch (Exception e) {
            builder.append(Lang.getMessage("commands.unable-to-deserialize", "text", message));
            e.printStackTrace();
        }
        return builder.create();
    }

    private static void processHoverTag(@NotNull ComponentBuilder builder, @NotNull String content) {
        Matcher m = HOVER_TAG_PATTERN.matcher(content);
        if (m.find()) {
            BaseComponent[] display = TextComponent.fromLegacyText(format(m.group(1)));
            BaseComponent[] hover = TextComponent.fromLegacyText(format(m.group(2)));
            builder.append(display)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
        }
    }

    private static void processClickTag(@NotNull ComponentBuilder builder, @NotNull String content) {
        Matcher m = CLICK_TAG_PATTERN.matcher(content);
        if (m.find()) {
            String action = m.group(1).trim().toUpperCase();
            String value = m.group(2).trim();

            try {
                ClickEvent.Action clickAction = ClickEvent.Action.valueOf(action);
                builder.event(new ClickEvent(clickAction, value));
            } catch (IllegalArgumentException e) {
                builder.append(Lang.getMessage("commands.unknown-click-action", "action", action));
                e.printStackTrace();
            }
        }
    }

    public static @NotNull String format(@NotNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendPageSwitch(@NotNull CommandSender sender, @NotNull List<LogEntry> entries, @Nullable String command) {
        sender.spigot().sendMessage(formatMessage("&a第 " + getCurrentPage(sender) + " / " + getMaxPage(entries) + " 页." + EMPTY_PLACEHOLDER + " <hover><d>&b<上一页></d><h>&b<上一页></h></hover><click><a>run_command</a><v>/sco page " + Math.max(1, getCurrentPage(sender) - 1) + "</v></click> <hover><d>&b<下一页></d><h>&b<下一页></h></hover><click><a>run_command</a><v>/sco page " + (Math.min(getCurrentPage(sender) + 1, getMaxPage(entries))) + "</v></click>"));
    }

    public void setup() {
        subCommands.add(new HelpCommand(plugin));
        subCommands.add(new ReloadCommand(plugin));
        subCommands.add(new VersionCommand(plugin));
        subCommands.add(new LookupCommand(plugin));
        subCommands.add(new PageCommand(plugin));
        subCommands.add(new DeleteCommand(plugin));
        subCommands.add(new InspectCommand(plugin));
    }

    public void registerCommand() {
        PluginCommand command = plugin.getCommand(ROOT_COMMAND);
        if (command == null) {
            plugin.getLogger().severe("Command \"" + ROOT_COMMAND + "\" not found!");
            return;
        } else {
            command.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Lang.getMessage("commands.no-permission"));
            return true;
        }

        for (SubCommand subCommand : subCommands) {
            if (subCommand.canCommand(sender, command, label, args)) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    subCommand.onCommand(sender, command, label, args);
                });
                return true;
            }
        }

        return defaultCommand.onCommand(sender, command, label, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> subCommandNames = subCommands.stream().map(SubCommand::getName).toList();
        if (args.length <= 1) {
            return subCommandNames;
        }

        List<String> completions = new ArrayList<>();
        for (SubCommand subCommand : subCommands) {
            List<String> subCompletion = subCommand.onTabComplete(sender, command, label, args);
            if (subCompletion != null) {
                completions.addAll(subCompletion);
            }
        }
        return completions;
    }
}
