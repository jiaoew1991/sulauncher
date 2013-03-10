package com.sulauncher.applicationbrowser;

import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

/**
 * An item in the list
 * @author jiaoew
 */
public class AllAppListItem {
    public ResolveInfo resolveInfo;
    public CharSequence label;
    public Drawable icon;
    public String packageName;
    public String className;
    public Bundle extras;
    
    public AllAppListItem(PackageManager pm, ResolveInfo resolveInfo) {
        this.resolveInfo = resolveInfo;
        label = resolveInfo.loadLabel(pm);
        ComponentInfo ci = resolveInfo.activityInfo;
        if (ci == null) ci = resolveInfo.serviceInfo;
        if (label == null && ci != null) {
            label = resolveInfo.activityInfo.name;
        }
        packageName = ci.applicationInfo.packageName;
        className = ci.name;
    }
    public AllAppListItem() {
    }
}
