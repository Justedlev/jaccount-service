package com.justedlev.account.common.jaudit;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JAudit {
    boolean ignore() default false;

    String comment() default "";

    Class<? extends JAuditValueConverter<?, ?>> valueConverter() default JAuditValueConverter.None.class;
}
