package com.boxshell.lesson.lxxz.wifi;

import android.graphics.drawable.Drawable;

/**
 * Created by yangjun on 16/3/30.
 */
public class FileListItem {
    public final Drawable fileIcon;       // the drawable for the ListView item ImageView
    public final String fileName;        // the text for the ListView item title

    FileListItem(Drawable icon, String name){
        this.fileIcon = icon;
        this.fileName = name;
    }

}
