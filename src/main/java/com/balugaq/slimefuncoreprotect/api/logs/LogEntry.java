package com.balugaq.slimefuncoreprotect.api.logs;

import com.balugaq.slimefuncoreprotect.api.utils.Debug;
import lombok.Data;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

@ToString
@Data
public class LogEntry {
    private static int BIT_MASK_ROLLBACKED = 1; // underline the message if it was rolled back
    private int id;
    private String player;
    private Timestamp time;
    private String action;
    private Location location;
    private String slimefunId;
    private String otherData;
    private int status;

    public static @NotNull String getStringBlockLocation(@Nullable Location location) {
        if (location == null) {
            return "unknown";
        }

        String string = location.getWorld().getName();
        string += ";" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        return string;
    }

    public static @NotNull String getStringLocation(@Nullable Location location) {
        if (location == null) {
            return "unknown";
        }
        return location.getWorld().getName() + ";" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
    }

    public boolean isRolledBack() {
        return (status & BIT_MASK_ROLLBACKED) != 0;
    }

    public void setTime(long time) {
        this.time = new Timestamp(time);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocation(@NotNull String location) {
        String[] loc = location.split(";");
        if (loc.length < 2) {
            return;
        }

        World world = Bukkit.getWorld(loc[0]);
        if (world == null) {
            Debug.debug("World not found: " + loc[0]);
            return;
        }

        String[] params = loc[1].split(":");
        if (params.length == 3) {
            setLocation(new Location(
                    world,
                    Double.parseDouble(params[0]),
                    Double.parseDouble(params[1]),
                    Double.parseDouble(params[2])
            ));
        } else if (params.length == 5) {
            setLocation(new Location(
                    world,
                    Double.parseDouble(params[0]),
                    Double.parseDouble(params[1]),
                    Double.parseDouble(params[2]),
                    Float.parseFloat(params[3]),
                    Float.parseFloat(params[4])
            ));
        }
    }
}
