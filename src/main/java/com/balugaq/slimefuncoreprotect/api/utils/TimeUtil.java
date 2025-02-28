package com.balugaq.slimefuncoreprotect.api.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Instant;

@UtilityClass
public class TimeUtil {
    public static @NotNull Timestamp now() {
        return Timestamp.from(Instant.now());
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

    // the opposite of getDelay()
    public static @Nullable Timestamp parseTime(@NotNull String timeStr) {
        if (timeStr.endsWith("s")) {
            double seconds = Double.parseDouble(timeStr.replace("s", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (seconds * 1000));
        } else if (timeStr.endsWith("m")) {
            double minutes = Double.parseDouble(timeStr.replace("m", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (minutes * 60 * 1000));
        } else if (timeStr.endsWith("h")) {
            double hours = Double.parseDouble(timeStr.replace("h", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (hours * 3600 * 1000));
        } else if (timeStr.endsWith("d")) {
            double days = Double.parseDouble(timeStr.replace("d", ""));
            return new Timestamp(System.currentTimeMillis() - (long) (days * 86400 * 1000));
        } else {
            return null;
        }
    }
}
