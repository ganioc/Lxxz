package com.boxshell.lesson.lxxz.online;

import android.graphics.drawable.Drawable;

/**
 * Created by yangjun on 16/3/6.
 */
public class FirmwareListItem {
    public final Drawable firmwareIcon;       // the drawable for the ListView item ImageView
    public final String firmwareName;        // the text for the ListView item title
    public final String firmwareDescrip;

    FirmwareListItem(Drawable icon, String name, String description){
        this.firmwareIcon = icon;
        this.firmwareName = name;
        this.firmwareDescrip = description;
    }
}
