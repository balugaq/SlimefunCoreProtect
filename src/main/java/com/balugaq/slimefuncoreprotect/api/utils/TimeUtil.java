package com.balugaq.slimefuncoreprotect.api.utils;

import lombok.experimental.UtilityClass;

import java.sql.Timestamp;
import java.time.Instant;

@UtilityClass
public class TimeUtil {
    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }
}
