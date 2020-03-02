package com.tzx.ams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-14 10:17
 * Description:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface TrackMethod {
    String tag();
}

