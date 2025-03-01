package com.balugaq.slimefuncoreprotect.api;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerQueryUser extends QueryUser {
    private final Player player;
    public PlayerQueryUser(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull String getUsername() {
        return "#player:" + player.getName();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public boolean isOp() {
        return player.isOp();
    }

    @Override
    public boolean hasPermission(String node) {
        return player.hasPermission(node);
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }
}
