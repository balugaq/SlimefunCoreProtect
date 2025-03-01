package com.balugaq.slimefuncoreprotect.api;

import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class ConsoleQueryUser extends QueryUser {
    private final ConsoleCommandSender console;
    public ConsoleQueryUser(ConsoleCommandSender console) {
        this.console = console;
    }

    @Override
    public @NotNull String getUsername() {
        return "#console:" + console.getName();
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isOp() {
        return console.isOp();
    }

    @Override
    public boolean hasPermission(String node) {
        return console.hasPermission(node);
    }
    @Override
    public void sendMessage(String message) {
        console.sendMessage(message);
    }
}
