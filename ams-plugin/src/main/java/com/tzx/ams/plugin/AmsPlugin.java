package com.tzx.ams.plugin;


import com.android.annotations.NonNull;
import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 16:40
 * Description:
 */
public class AmsPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        assert appExtension != null;
        appExtension.registerTransform(new AmsTransform(project));

    }
}
