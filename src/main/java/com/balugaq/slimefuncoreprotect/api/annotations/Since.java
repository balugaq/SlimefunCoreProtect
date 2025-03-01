package com.balugaq.slimefuncoreprotect.api.annotations;

import com.balugaq.slimefuncoreprotect.api.enums.ConfigVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to notice the appearance of version of the configuration section
 *
 * @author balugaq
 * @see ConfigVersion
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Since {
    ConfigVersion value();
}
